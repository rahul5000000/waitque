package com.rrsgroup.waitque.util

import com.rrsgroup.waitque.domain.UserRole
import org.springframework.security.oauth2.jwt.Jwt
import spock.lang.Specification

class JwtWrapperSpec extends Specification {
    def "getFirstName should return the first name from the JWT token"() {
        given:
        def jwt = getMockJwt()
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getFirstName()

        then:
        result == "first_name"
    }

    def "getLastName should return the last name from the JWT token"() {
        given:
        def jwt = getMockJwt()
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getLastName()

        then:
        result == "last_name"
    }

    def "getEmail should return the email from the JWT token"() {
        given:
        def jwt = getMockJwt()
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getEmail()

        then:
        result == "email@test.com"
    }

    def "getUsername should return the username from the JWT token"() {
        given:
        def jwt = getMockJwt()
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getUsername()

        then:
        result == "username"
    }

    def "getRealmRoles should return the realm roles from the JWT token"() {
        given:
        def jwt = getMockJwt()
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getRealmRoles()

        then:
        result != null
        result.size() == 2
        result.get(0) == "default-roles-rrs-waitque"
        result.get(1) == "ADMIN"
    }

    def "getRealmRoles should return empty list for null realm roles in JWT token"() {
        given:
        def jwt = getMockJwt(false, false, false)
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getRealmRoles()

        then:
        result != null
        result.size() == 0
    }

    def "getRealmRoles should return empty list for no roles"() {
        given:
        def jwt = getMockJwt(true, false, false)
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getRealmRoles()

        then:
        result != null
        result.size() == 0
    }

    def "getUserRole should return the user roles from the JWT token"() {
        given:
        def jwt = getMockJwt()
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getUserRole()

        then:
        result.isPresent()
        result.get() == UserRole.ADMIN
    }

    def "getUserRole should return empty Optional is realm roles does not contain User Role"() {
        given:
        def jwt = getMockJwt(true, true, false)
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getUserRole()

        then:
        result.isEmpty()
    }

    private Jwt getMockJwt() {
        return getMockJwt(true, true, true)
    }

    private Jwt getMockJwt(boolean includeRealmAccess, boolean includeRoles, boolean includeUserRole) {
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
