package com.rrsgroup.waitque.dto;

import com.rrsgroup.waitque.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class UserDto {
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String username;
    protected UserRole role;
}
