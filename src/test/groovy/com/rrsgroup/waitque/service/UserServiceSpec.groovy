package com.rrsgroup.waitque.service

import com.rrsgroup.waitque.domain.UserRole
import com.rrsgroup.waitque.exception.RoleNotFoundException
import com.rrsgroup.waitque.util.MockGenerator
import spock.lang.Specification

class UserServiceSpec extends Specification {
    def mockGenerator = new MockGenerator()
    def service = new UserService()

    def "mapJwtToUser returns mapped user for complete JWT"() {
        given:
        def jwt = mockGenerator.getMockJwt()

        when:
        def result = service.mapJwtToUser(jwt)

        then:
        result != null
        result.firstName() == "first_name"
        result.lastName() == "last_name"
        result.email() == "email@test.com"
        result.username() == "username"
        result.role() == UserRole.ADMIN
    }

    def "mapJwtToUser throws RoleNotFoundException exception for JWT without UserRole"() {
        given:
        def jwt = mockGenerator.getMockJwt(false, false, false)

        when:
        def result = service.mapJwtToUser(jwt)

        then:
        thrown(RoleNotFoundException)
    }
}
