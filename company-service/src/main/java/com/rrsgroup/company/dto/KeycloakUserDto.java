package com.rrsgroup.company.dto;

import com.rrsgroup.company.domain.KeycloakRole;

public record KeycloakUserDto(String userId, String username, String firstName, String lastName, String email, KeycloakRole role, KeycloakStatus status) {
}
