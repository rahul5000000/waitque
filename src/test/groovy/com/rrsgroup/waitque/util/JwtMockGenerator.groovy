package com.rrsgroup.waitque.util

import org.springframework.security.oauth2.jwt.Jwt
import spock.lang.Specification

class JwtMockGenerator extends Specification {
    Jwt getMockJwt() {
        return getMockJwt(true, true, true)
    }

    Jwt getMockJwt(boolean includeRealmAccess, boolean includeRoles, boolean includeUserRole) {
        def realmAccess = null
        if(includeRealmAccess) {
            realmAccess = new HashMap()

            if(includeRoles) {
                def roles = new ArrayList()
                roles.add("default-roles-rrs-waitque")

                if(includeUserRole) {
                    roles.add("ADMIN")
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

        return jwt
    }
}
