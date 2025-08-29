package com.rrsgroup.waitque.util

import com.rrsgroup.waitque.domain.UserRole
import com.rrsgroup.waitque.dto.AdminUserDto
import com.rrsgroup.waitque.dto.SuperUserDto
import com.rrsgroup.waitque.dto.UserDto
import com.rrsgroup.waitque.entity.Address
import com.rrsgroup.waitque.entity.Company
import com.rrsgroup.waitque.entity.PhoneNumber
import org.apache.commons.lang3.NotImplementedException
import org.springframework.security.oauth2.jwt.Jwt
import spock.lang.Specification

class MockGenerator extends Specification {
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

    Company getCompanyMock() {
        def companyId = 1231L
        def name = "name"
        def logoUrl = "logoUrl.com"
        def landingPrompt = "What do you want to do?"
        def textColor = "#FFFFFF"
        def backgroundColor = "#000000"
        def primaryButtonColor = "#AAAAAA"
        def secondaryButtonColor = "#BBBBBB"
        def warningButtonColor = "#CCCCCC"
        def dangerButtonColor = "#DDDDDD"

        def addressId = 1232L
        def address1 = "address1"
        def address2 = "address2"
        def city = "city"
        def state = "state"
        def zipcode = "zipcode"
        def country = "country"

        def phoneNumberId = 1233L
        def countryCode = 1
        def phoneNumber = 1234567

        def address = new Address(addressId, address1, address2, city, state, zipcode, country)
        def phoneNumberDomain = new PhoneNumber(phoneNumberId, countryCode, phoneNumber)

        return new Company(companyId, name, logoUrl, landingPrompt, textColor, backgroundColor, primaryButtonColor,
                secondaryButtonColor, warningButtonColor, dangerButtonColor, address, phoneNumberDomain)
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
