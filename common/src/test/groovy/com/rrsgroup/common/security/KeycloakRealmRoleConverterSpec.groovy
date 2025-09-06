package com.rrsgroup.common.security

import com.rrsgroup.common.util.CommonMockGenerator
import spock.lang.Specification

class KeycloakRealmRoleConverterSpec extends Specification {
    def mockGenerator = new CommonMockGenerator()

    def "convert returns realm roles mapped to set of GrantedAuthority"() {
        given:
        def jwt = mockGenerator.getMockJwt()
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
        def jwt = mockGenerator.getMockJwt(false, false, false)
        def converter = new KeycloakRealmRoleConverter()

        when:
        def result = converter.convert(jwt)

        then:
        result != null
        result.size() == 0
    }
}
