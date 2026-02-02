package com.rrsgroup.company.service;

import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.company.domain.KeycloakRole;
import com.rrsgroup.company.dto.KeycloakStatus;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {
    private static final String REALM = "rrs-waitque";
    private final Keycloak keycloak;

    @Autowired
    public KeycloakService(Keycloak keycloak) {
        this.keycloak = keycloak;
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

        // TODO: Generate a random password
        setPassword(userId, "Welcome123!", true);
        assignRealmRole(userId, KeycloakRole.FIELD_USER);
        UserRepresentation newUser = getUserById(userId);

        // TODO: Send welcome email with password reset link

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
    }
}
