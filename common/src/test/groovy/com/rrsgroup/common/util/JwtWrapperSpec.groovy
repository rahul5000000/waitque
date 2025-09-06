package com.rrsgroup.common.util

import spock.lang.Specification

class JwtWrapperSpec extends Specification {
    def mockGenerator = new CommonMockGenerator()

    def "getFirstName should return the first name from the JWT token"() {
        given:
        def jwt = mockGenerator.getMockJwt()
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getFirstName()

        then:
        result == "first_name"
    }

    def "getLastName should return the last name from the JWT token"() {
        given:
        def jwt = mockGenerator.getMockJwt()
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getLastName()

        then:
        result == "last_name"
    }

    def "getEmail should return the email from the JWT token"() {
        given:
        def jwt = mockGenerator.getMockJwt()
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getEmail()

        then:
        result == "email@test.com"
    }

    def "getUsername should return the username from the JWT token"() {
        given:
        def jwt = mockGenerator.getMockJwt()
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getUsername()

        then:
        result == "username"
    }

    def "getRealmRoles should return the realm roles from the JWT token"() {
        given:
        def jwt = mockGenerator.getMockJwt()
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
        def jwt = mockGenerator.getMockJwt(false, false, false)
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getRealmRoles()

        then:
        result != null
        result.size() == 0
    }

    def "getRealmRoles should return empty list for no roles"() {
        given:
        def jwt = mockGenerator.getMockJwt(true, false, false)
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getRealmRoles()

        then:
        result != null
        result.size() == 0
    }

    def "getUserRole should return the user roles from the JWT token"() {
        given:
        def jwt = mockGenerator.getMockJwt()
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getUserRole()

        then:
        result.isPresent()
        result.get() == UserRole.ADMIN
    }

    def "getUserRole should return empty Optional is realm roles does not contain User Role"() {
        given:
        def jwt = mockGenerator.getMockJwt(true, true, false)
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getUserRole()

        then:
        result.isEmpty()
    }

    def "getCompanyId #description"() {
        given:
        def jwt = mockGenerator.getMockJwt(true, true, true, includeCompanyId, companyId)
        def jwtWrapper = new JwtWrapper(jwt)

        when:
        def result = jwtWrapper.getCompanyId()

        then:
        if(expected) {
            result.isPresent()
            result.get() == 1L
        } else {
            result.isEmpty()
        }

        where:
        companyId | includeCompanyId | expected | description
        1L        | true             | true     | "returns companyId if it's included in JWT"
        1L        | false            | false    | "returns empty optional companyId is not in JWT"
        null      | true             | false    | "returns empty optional companyId is exists in JWT, but is null"
    }
}
