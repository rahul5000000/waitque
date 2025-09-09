package com.rrsgroup.common.dto;

import com.rrsgroup.common.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class UserDto {
    protected String userId;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String username;
    protected UserRole role;
}
