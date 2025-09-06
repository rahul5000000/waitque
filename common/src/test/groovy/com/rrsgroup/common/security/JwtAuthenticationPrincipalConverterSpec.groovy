package com.rrsgroup.common.security

import com.rrsgroup.common.domain.UserRole
import com.rrsgroup.common.dto.AdminUserDto
import com.rrsgroup.common.dto.SuperUserDto
import com.rrsgroup.common.dto.UserDto
import com.rrsgroup.common.exception.RoleNotFoundException
import com.rrsgroup.common.util.CommonMockGenerator
import spock.lang.Specification

class JwtAuthenticationPrincipalConverterSpec extends Specification {
    def mockGenerator = new CommonMockGenerator()

    def "convert returns ADMIN principal for admin user's JWT"() {
        given:
        def jwt = mockGenerator.getMockJwt("ADMIN")
        def converter = new JwtAuthenticationPrincipalConverter()

        when:
        def result = converter.convert(jwt)

        then:
        result.getPrincipal() instanceof AdminUserDto
        ((UserDto)result.getPrincipal()).firstName == "first_name"
        ((UserDto)result.getPrincipal()).lastName == "last_name"
        ((UserDto)result.getPrincipal()).email == "email@test.com"
        ((UserDto)result.getPrincipal()).username == "username"
        ((UserDto)result.getPrincipal()).role == UserRole.ADMIN
        ((AdminUserDto)result.getPrincipal()).companyId == 1L
        result.getAuthorities().size() == 2
        result.getAuthorities().getAt(0).authority == "ROLE_DEFAULT-ROLES-RRS-WAITQUE"
        result.getAuthorities().getAt(1).authority == "ROLE_ADMIN"
    }

    def "convert returns SUPERUSER principal for superuser's JWT"() {
        given:
        def jwt = mockGenerator.getMockJwt("SUPERUSER")
        def converter = new JwtAuthenticationPrincipalConverter()

        when:
        def result = converter.convert(jwt)

        then:
        result.getPrincipal() instanceof SuperUserDto
        ((UserDto)result.getPrincipal()).firstName == "first_name"
        ((UserDto)result.getPrincipal()).lastName == "last_name"
        ((UserDto)result.getPrincipal()).email == "email@test.com"
        ((UserDto)result.getPrincipal()).username == "username"
        ((UserDto)result.getPrincipal()).role == UserRole.SUPERUSER
        result.getAuthorities().size() == 2
        result.getAuthorities().getAt(0).authority == "ROLE_SUPERUSER"
        result.getAuthorities().getAt(1).authority == "ROLE_DEFAULT-ROLES-RRS-WAITQUE"
    }

    def "convert throws RoleNotFoundException exception for JWT without UserRole"() {
        given:
        def jwt = mockGenerator.getMockJwt(false, false, false)
        def converter = new JwtAuthenticationPrincipalConverter()

        when:
        def result = converter.convert(jwt)

        then:
        thrown(RoleNotFoundException)
    }

    def "convert throws IllegalStateException exception for admin user's JWT without company_id claim"() {
        given:
        def jwt = mockGenerator.getMockJwt("ADMIN", true, true, true, false, 1L)
        def converter = new JwtAuthenticationPrincipalConverter()

        when:
        def result = converter.convert(jwt)

        then:
        thrown(IllegalStateException)
    }
}
