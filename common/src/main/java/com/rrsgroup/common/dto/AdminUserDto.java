package com.rrsgroup.common.dto;

import com.rrsgroup.common.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUserDto extends UserDto {
    private Long companyId;

    public AdminUserDto(String userId, String firstName, String lastName, String email, String username, UserRole role, Long companyId) {
        super(userId, firstName, lastName, email, username, role);
        this.companyId = companyId;
    }
}
