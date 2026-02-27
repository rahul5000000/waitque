package com.rrsgroup.company.service;

import com.rrsgroup.common.EmailRequest;
import com.rrsgroup.common.domain.EmailTemplate;
import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.common.dto.SuperUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.common.service.EmailService;
import com.rrsgroup.company.domain.KeycloakRole;
import com.rrsgroup.company.dto.KeycloakStatus;
import com.rrsgroup.company.entity.Company;
import jakarta.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

@Log4j2
@Service
public class KeycloakService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String LETTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!#$&";
    private static final String ALL = LETTERS + NUMBERS + SYMBOLS;

    private static final String REALM = "rrs-waitque";
    private final Keycloak keycloak;
    private final NotificationService notificationService;

    @Autowired
    public KeycloakService(Keycloak keycloak, NotificationService notificationService) {
        this.keycloak = keycloak;
        this.notificationService = notificationService;
    }

    public List<UserRepresentation> getAllUsers(AdminUserDto adminUser) {
        return keycloak.realm(REALM).users().searchByAttributes("companyId:"+adminUser.getCompanyId());
    }

    private UserRepresentation getUserById(String userId) {
        return keycloak.realm(REALM)
                .users()
                .get(userId)
                .toRepresentation();
    }

    public UserRepresentation createFieldUser(String username, String email, String firstName, String lastName, AdminUserDto createdBy) {
        String userId = createUser(username, email, firstName, lastName, createdBy.getCompanyId(), KeycloakRole.FIELD_USER);

        String temporaryPassword = generatePassword(8);
        setPassword(userId, temporaryPassword, true);
        assignRealmRole(userId, KeycloakRole.FIELD_USER);
        UserRepresentation newUser = getUserById(userId);

        // Send welcome email
        notificationService.notifyNewUser(email, firstName, lastName, username, temporaryPassword, createdBy);

        return newUser;
    }

    public UserRepresentation createAdminUser(String username, String email, String firstName, String lastName, Company company, SuperUserDto createdBy) {
        String userId = createUser(username, email, firstName, lastName, company.getId(), KeycloakRole.ADMIN);

        String temporaryPassword = generatePassword(8);
        setPassword(userId, temporaryPassword, true);
        assignRealmRole(userId, KeycloakRole.ADMIN);
        UserRepresentation newUser = getUserById(userId);

        // Send welcome email
        notificationService.notifyNewAdminUser(email, firstName, lastName, username, temporaryPassword, company, createdBy);

        return newUser;
    }

    private String createUser(String username, String email, String firstName, String lastName, Long companyId, KeycloakRole role) {

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(true);

        // Custom attribute
        user.setAttributes(Map.of(
                "companyId", List.of(companyId.toString()),
                "role", List.of(role.toString())
        ));

        Response response = keycloak.realm(REALM)
                .users()
                .create(user);

        if(response.getStatus() == 409) {
            throw new IllegalUpdateException("User with the same username or email already exists");
        } else if(response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user: " + response.getStatus());
        }

        // Extract userId from Location header
        String userId = response.getLocation()
                .getPath()
                .replaceAll(".*/([^/]+)$", "$1");

        response.close();
        return userId;
    }

    private void setPassword(String userId, String password, boolean temporary) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(temporary);

        keycloak.realm(REALM)
                .users()
                .get(userId)
                .resetPassword(credential);
    }

    private void assignRealmRole(String userId, KeycloakRole realmRole) {
        RoleRepresentation role =
                keycloak.realm(REALM)
                        .roles()
                        .get(realmRole.toString())
                        .toRepresentation();

        keycloak.realm(REALM)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(role));
    }

    public void setEnabled(String userId, KeycloakStatus status, AdminUserDto adminUser) {
        UsersResource users = keycloak.realm(REALM).users();
        UserResource user = users.get(userId);

        UserRepresentation rep = user.toRepresentation();

        if(rep.getAttributes().get("companyId") == null ||
                !rep.getAttributes().get("companyId").get(0).equals(adminUser.getCompanyId().toString())) {
            throw new IllegalRequestException("User does not belong to the admin's company");
        }

        rep.setEnabled(status.isEnabled());

        user.update(rep);

        notificationService.notifyUserStatusChanged(rep.getEmail(), rep.getFirstName(), rep.getLastName(), status, adminUser);
    }

    public void deleteUser(String userId, AdminUserDto adminUser) {
        UserResource user = keycloak.realm(REALM)
                .users()
                .get(userId);

        UserRepresentation rep = user.toRepresentation();

        if(rep.getAttributes().get("companyId") == null ||
                !rep.getAttributes().get("companyId").get(0).equals(adminUser.getCompanyId().toString())) {
            throw new IllegalRequestException("User does not belong to the admin's company");
        }

        if(rep.isEnabled()) {
            throw new IllegalRequestException("Cannot delete an enabled user. Please disable the user first.");
        }

        user.remove();

        notificationService.notifyUserDeleted(rep.getFirstName(), rep.getLastName(), adminUser);
    }

    private String generatePassword(int length) {
        if (length < 3) {
            throw new IllegalArgumentException("Password length must be at least 3");
        }

        List<Character> chars = new ArrayList<>();

        // Ensure minimum complexity
        chars.add(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
        chars.add(NUMBERS.charAt(RANDOM.nextInt(NUMBERS.length())));
        chars.add(SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length())));

        // Fill remaining characters
        for (int i = 3; i < length; i++) {
            chars.add(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }

        // Shuffle for randomness
        Collections.shuffle(chars, RANDOM);

        StringBuilder password = new StringBuilder(length);
        chars.forEach(password::append);

        return password.toString();
    }

    public void resetUserPassword(String userId, AdminUserDto adminUser) {
        UserResource user = keycloak.realm(REALM)
                .users()
                .get(userId);

        UserRepresentation rep = user.toRepresentation();

        if(rep.getAttributes().get("companyId") == null ||
                !rep.getAttributes().get("companyId").get(0).equals(adminUser.getCompanyId().toString())) {
            throw new IllegalRequestException("User does not belong to the admin's company");
        }

        if(!rep.isEnabled()) {
            throw new IllegalRequestException("Cannot reset the password of a disabled user. Please enable the user first.");
        }

        String temporaryPassword = generatePassword(8);
        setPassword(userId, temporaryPassword, true);

        notificationService.notifyPasswordReset(rep.getEmail(), rep.getFirstName(), rep.getLastName(), temporaryPassword, adminUser);
    }
}
