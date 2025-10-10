package com.rrsgroup.common.dto;

import com.rrsgroup.common.domain.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class CompanyUserDto extends UserDto {
    private Long companyId;

    public CompanyUserDto(String userId, String firstName, String lastName, String email, String username, UserRole role, Long companyId) {
        super(userId, firstName, lastName, email, username, role);
        this.companyId = companyId;
    }
}
