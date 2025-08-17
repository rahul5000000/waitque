package com.rrsgroup.waitque.dto;

import com.rrsgroup.waitque.domain.UserRole;

public record UserDto(String firstName, String lastName, String email, String username, UserRole role) {
}
