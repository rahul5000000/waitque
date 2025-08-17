package com.rrsgroup.waitque.security

import org.springframework.security.oauth2.jwt.Jwt
import spock.lang.Specification

class KeycloakRealmRoleConverterSpec extends Specification {
    def "convert returns realm roles mapped to set of GrantedAuthority"() {
        given:
        def jwt = getMockJwt()
        def converter = new KeycloakRealmRoleConverter()

        when:
        def result = converter.convert(jwt)

        then:
        result != null
        result.size() == 2
        result.getAt(0).authority == "ROLE_DEFAULT-ROLES-RRS-WAITQUE"
        result.getAt(1).authority == "ROLE_ADMIN"
    }

    def "convert returns empty set for JWT without roles"() {
        given:
        def jwt = getMockJwt(false)
        def converter = new KeycloakRealmRoleConverter()

        when:
        def result = converter.convert(jwt)

        then:
        result != null
        result.size() == 0
    }

    private Jwt getMockJwt() {
        return getMockJwt(true)
    }

    private Jwt getMockJwt(boolean includeRealmAccess) {
        def realmAccess = null
        if(includeRealmAccess) {
            realmAccess = new HashMap()

            def roles = new ArrayList()
            roles.add("default-roles-rrs-waitque")
            roles.add("ADMIN")
            realmAccess.put("roles", roles)
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
