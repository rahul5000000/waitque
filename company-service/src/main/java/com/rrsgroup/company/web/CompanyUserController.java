package com.rrsgroup.company.web;

import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.dto.SuperUserDto;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.company.dto.KeycloakStatus;
import com.rrsgroup.company.dto.KeycloakUserDto;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.service.CompanyService;
import com.rrsgroup.company.service.KeycloakService;
import com.rrsgroup.company.service.KeycloakUserDtoMapper;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class CompanyUserController {
    private final KeycloakService keycloakService;
    private final KeycloakUserDtoMapper keycloakUserDtoMapper;
    private final CompanyService companyService;

    @Autowired
    public CompanyUserController(
            KeycloakService keycloakService,
            KeycloakUserDtoMapper keycloakUserDtoMapper,
            CompanyService companyService) {
        this.keycloakUserDtoMapper = keycloakUserDtoMapper;
        this.keycloakService = keycloakService;
        this.companyService = companyService;
    }

    @GetMapping("/api/admin/company/users")
    public List<KeycloakUserDto> getAllUsersInCompany(@AuthenticationPrincipal AdminUserDto user) {
        List<UserRepresentation> allUsers = keycloakService.getAllUsers(user);
        return keycloakUserDtoMapper.map(allUsers.stream().filter(keyCloakUser -> !keyCloakUser.getUsername().contains("waitque")).toList());
    }

    @PostMapping("/api/admin/company/users/field")
    public KeycloakUserDto createFieldUser(@AuthenticationPrincipal AdminUserDto user, @RequestBody KeycloakUserDto request) {
        return keycloakUserDtoMapper.map(
                keycloakService.createFieldUser(
                        request.username(),
                        request.email(),
                        request.firstName(),
                        request.lastName(),
                        user
                )
        );
    }

    @PostMapping("/api/internal/companies/{companyId}/users/admin")
    public KeycloakUserDto createAdminUser(
            @AuthenticationPrincipal SuperUserDto user,
            @PathVariable(name = "companyId") Long companyId,
            @RequestBody KeycloakUserDto request) {
        Optional<Company> companyOptional = companyService.getCompany(companyId);

        if(companyOptional.isEmpty()) {
            throw new RecordNotFoundException("Company not found with that ID");
        }

        return keycloakUserDtoMapper.map(
                keycloakService.createAdminUser(
                        request.username(),
                        request.email(),
                        request.firstName(),
                        request.lastName(),
                        companyOptional.get(),
                        user
                )
        );
    }

    @PatchMapping("/api/admin/company/users/{userId}/status/{status}")
    public void updateUserStatus(
            @AuthenticationPrincipal AdminUserDto adminUser,
            @PathVariable("userId") String userId,
            @PathVariable("status") KeycloakStatus status) {
        keycloakService.setEnabled(userId, status, adminUser);
    }

    @DeleteMapping("/api/admin/company/users/{userId}")
    public void deleteUser(
            @AuthenticationPrincipal AdminUserDto adminUser,
            @PathVariable("userId") String userId) {
        keycloakService.deleteUser(userId, adminUser);
    }

    @PostMapping("/api/admin/company/users/{userId}/password/reset")
    public void resetUserPassword(@AuthenticationPrincipal AdminUserDto adminUser,
                                  @PathVariable("userId") String userId) {
        keycloakService.resetUserPassword(userId, adminUser);
    }
}
