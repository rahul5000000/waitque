package com.rrsgroup.common.dto;

import com.rrsgroup.common.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuperUserDto extends UserDto {
    public SuperUserDto(String firstName, String lastName, String email, String username, UserRole role) {
        super(firstName, lastName, email, username, role);
    }
}
