package com.rrsgroup.company.web;

import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.company.dto.KeycloakStatus;
import com.rrsgroup.company.dto.KeycloakUserDto;
import com.rrsgroup.company.service.KeycloakService;
import com.rrsgroup.company.service.KeycloakUserDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CompanyUserController {
    private final KeycloakService keycloakService;
    private final KeycloakUserDtoMapper keycloakUserDtoMapper;

    @Autowired
    public CompanyUserController(KeycloakService keycloakService, KeycloakUserDtoMapper keycloakUserDtoMapper) {
        this.keycloakUserDtoMapper = keycloakUserDtoMapper;
        this.keycloakService = keycloakService;
    }

    @GetMapping("/api/admin/company/users")
    public List<KeycloakUserDto> getAllUsersInCompany(@AuthenticationPrincipal AdminUserDto user) {
        return keycloakUserDtoMapper.map(keycloakService.getAllUsers(user));
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
}
