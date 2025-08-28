package com.rrsgroup.waitque.dto;

import com.rrsgroup.waitque.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUserDto extends UserDto {
    private Long companyId;

    public AdminUserDto(String firstName, String lastName, String email, String username, UserRole role, Long companyId) {
        super(firstName, lastName, email, username, role);
        this.companyId = companyId;
    }
}
