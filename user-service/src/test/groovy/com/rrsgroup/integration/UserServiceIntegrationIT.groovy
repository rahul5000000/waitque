package com.rrsgroup.integration

import groovy.json.JsonSlurper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

class UserServiceIntegrationIT extends IntegrationTestSpec {
    def userServiceBase = "http://localhost:8084"

    def restTemplate = new RestTemplate()

    def "should get Admin user info using Keycloak token"() {
        when: "a token is requested from Keycloak"
        def token = getAdminAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/users/me endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(
                "${userServiceBase}/api/users/me",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the user info"
        response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        json.firstName == "Admin"
        json.lastName == "User"
        json.email == "admin@test.com"
        json.username == "admintest1"
        json.role == "ADMIN"
        json.companyId == 1
    }

    def "should get Field user info using Keycloak token"() {
        when: "a token is requested from Keycloak"
        def token = getFieldUserAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/users/me endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(
                "${userServiceBase}/api/users/me",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the user info"
        response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        json.firstName == "Field"
        json.lastName == "User"
        json.email == "field@test.com"
        json.username == "fieldtest1"
        json.role == "FIELD_USER"
        json.companyId == 1
    }

    def "should get Super user info using Keycloak token"() {
        when: "a token is requested from Keycloak"
        def token = getSuperUserAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/users/me endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(
                "${userServiceBase}/api/users/me",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the user info"
        response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        json.firstName == "Super"
        json.lastName == "User"
        json.email == "superuser@test.com"
        json.username == "superuser"
        json.role == "SUPERUSER"
        json.companyId == null
    }
}
