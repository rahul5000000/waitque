package com.rrsgroup.waitque.service

import com.rrsgroup.waitque.domain.UserRole
import com.rrsgroup.waitque.dto.AdminUserDto
import com.rrsgroup.waitque.dto.SuperUserDto
import com.rrsgroup.waitque.exception.RoleNotFoundException
import com.rrsgroup.waitque.util.MockGenerator
import spock.lang.Specification

class UserServiceSpec extends Specification {
    def mockGenerator = new MockGenerator()
    def service = new UserService()

    def "mapJwtToUser returns mapped ADMIN user for complete JWT"() {
        given:
        def jwt = mockGenerator.getMockJwt("ADMIN")

        when:
        def result = service.mapJwtToUser(jwt)

        then:
        result != null
        result instanceof AdminUserDto
        result.firstName == "first_name"
        result.lastName == "last_name"
        result.email == "email@test.com"
        result.username == "username"
        result.role == UserRole.ADMIN
        ((AdminUserDto)result).companyId == 1L
    }

    def "mapJwtToUser returns mapped SUPERUSER user for complete JWT"() {
        given:
        def jwt = mockGenerator.getMockJwt("SUPERUSER")

        when:
        def result = service.mapJwtToUser(jwt)

        then:
        result != null
        result instanceof SuperUserDto
        result.firstName == "first_name"
        result.lastName == "last_name"
        result.email == "email@test.com"
        result.username == "username"
        result.role == UserRole.SUPERUSER
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
