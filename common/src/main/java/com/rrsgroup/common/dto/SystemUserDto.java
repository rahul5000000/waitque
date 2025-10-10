package com.rrsgroup.common.dto;

import com.rrsgroup.common.domain.UserRole;

public class SystemUserDto extends UserDto {
    public SystemUserDto(String userId, String firstName, String lastName, String email, String username, UserRole role) {
        super(userId, firstName, lastName, email, username, role);
    }
}
