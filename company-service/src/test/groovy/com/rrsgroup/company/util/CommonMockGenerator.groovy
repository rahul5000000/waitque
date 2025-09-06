package com.rrsgroup.company.util

import com.rrsgroup.common.domain.UserRole
import com.rrsgroup.common.dto.AdminUserDto
import com.rrsgroup.common.dto.SuperUserDto
import com.rrsgroup.common.dto.UserDto
import org.apache.commons.lang3.NotImplementedException
import org.springframework.security.oauth2.jwt.Jwt
import spock.lang.Specification

class CommonMockGenerator extends Specification {
    Jwt getMockJwt() {
        return getMockJwt("ADMIN", true, true, true)
    }

    Jwt getMockJwt(String role) {
        return getMockJwt(role, true, true, true)
    }

    Jwt getMockJwt(boolean includeRealmAccess, boolean includeRoles, boolean includeUserRole) {
        return getMockJwt("ADMIN", includeRealmAccess, includeRoles, includeUserRole, true, 1L)
    }

    Jwt getMockJwt(String role, boolean includeRealmAccess, boolean includeRoles, boolean includeUserRole) {
        return getMockJwt(role, includeRealmAccess, includeRoles, includeUserRole, true, 1L)
    }

    Jwt getMockJwt(String role, boolean includeRealmAccess, boolean includeRoles, boolean includeUserRole, boolean includeCompanyId, Long companyId) {
        def realmAccess = null
        if(includeRealmAccess) {
            realmAccess = new HashMap()

            if(includeRoles) {
                def roles = new ArrayList()
                roles.add("default-roles-rrs-waitque")

                if(includeUserRole) {
                    roles.add(role)
                }

                realmAccess.put("roles", roles)
            }
        }

        def jwt = Mock(Jwt.class)
        jwt.getClaim("given_name") >> "first_name"
        jwt.getClaim("family_name") >> "last_name"
        jwt.getClaim("email") >> "email@test.com"
        jwt.getClaim("preferred_username") >> "username"
        jwt.getClaim("realm_access") >> realmAccess

        if(includeCompanyId) jwt.getClaim("company_id") >> companyId

        return jwt
    }

    UserDto getUserMock(UserRole role) {
        def firstName = "firstName"
        def lastName = "lastName"
        def email = "email"
        def username = "username"
        def companyId = 1231L

        if(role == UserRole.ADMIN) {
            return new AdminUserDto(firstName, lastName, email, username, role, companyId)
        } else if (role == UserRole.SUPERUSER) {
            return new SuperUserDto(firstName, lastName, email, username, role)
        } else {
            throw new NotImplementedException("User of type " + role + " is not supported")
        }
    }
}

