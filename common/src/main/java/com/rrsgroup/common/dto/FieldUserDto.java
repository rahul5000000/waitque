package com.rrsgroup.common.dto;

import com.rrsgroup.common.domain.UserRole;
import lombok.Data;

@Data
public class FieldUserDto extends CompanyUserDto {

    public FieldUserDto(String userId, String firstName, String lastName, String email, String username, Long companyId) {
        super(userId, firstName, lastName, email, username, UserRole.FIELD_USER, companyId);
    }
}
