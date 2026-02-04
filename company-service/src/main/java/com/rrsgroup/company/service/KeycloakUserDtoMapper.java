package com.rrsgroup.company.service;

import com.rrsgroup.company.domain.KeycloakRole;
import com.rrsgroup.company.dto.KeycloakStatus;
import com.rrsgroup.company.dto.KeycloakUserDto;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeycloakUserDtoMapper {
    public List<KeycloakUserDto> map(List<UserRepresentation> userRepresentations) {
        return userRepresentations.stream()
                .map(this::map)
                .toList();
    }

    public KeycloakUserDto map(UserRepresentation userRepresentation) {
        return new KeycloakUserDto(
                userRepresentation.getId(),
                userRepresentation.getUsername(),
                userRepresentation.getFirstName(),
                userRepresentation.getLastName(),
                userRepresentation.getEmail(),
                userRepresentation.getAttributes() != null && userRepresentation.getAttributes().get("role") != null
                        ? KeycloakRole.valueOf(userRepresentation.getAttributes().get("role").get(0))
                        : null,
                userRepresentation.isEnabled() ? KeycloakStatus.ENABLED : KeycloakStatus.DISABLED
        );
    }
}
