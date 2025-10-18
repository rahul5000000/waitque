package com.rrsgroup.integration

import groovy.json.JsonSlurper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

class CompanyControllerIT extends IntegrationTestSpec {
    def companyServiceBase = "http://localhost:8082"
    def restTemplate = new RestTemplate()

    def setup() {
        initTestCompany()
    }

    def "Super User should get list of companies"() {
        when: "a token is requested from Keycloak"
        def token = getSuperUserAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/internal/companies endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(
                "${companyServiceBase}/api/internal/companies?limit=10&page=0&sortDir=DESC&sortField=id",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the paginated response of companies"
        response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        def expectedJson = new JsonSlurper().parseText("{\"page\":0,\"limit\":10,\"total\":1,\"sortField\":\"id\",\"sortDir\":\"DESC\",\"companies\":[{\"id\":1,\"name\":\"Test Company\",\"logoUrl\":\"logoUrl.com\"}]}")
        json == expectedJson
    }

    def "Non-Super User should get 403 when getting list of companies"() {
        when: "a token is requested from Keycloak"
        def token = getNonSuperUserAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/internal/companies endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def exception

        try {
            def response = restTemplate.exchange(
                    "${companyServiceBase}/api/internal/companies?limit=10&page=0&sortDir=DESC&sortField=id",
                    HttpMethod.GET,
                    request,
                    String
            )
        } catch (Exception e) {
            exception = e
        }

        then: "the response is Forbidden"
        exception != null
        exception.statusCode == HttpStatus.FORBIDDEN
    }

    def "Super User should get specific company"() {
        when: "a token is requested from Keycloak"
        def token = getSuperUserAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/internal/companies/{companyId} endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(
                "${companyServiceBase}/api/internal/companies/1",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the company details"
        response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        def expectedJson = new JsonSlurper().parseText("{\"id\":1,\"name\":\"Test Company\",\"address\":{\"id\":1,\"address1\":\"123 Main St.\",\"address2\":null,\"city\":\"Marietta\",\"state\":\"Georgia\",\"zipcode\":\"30068\",\"country\":null},\"phoneNumber\":{\"id\":1,\"countryCode\":1,\"phoneNumber\":1231231234},\"logoUrl\":\"logoUrl.com\",\"landingPrompt\":\"What do you want to do?\",\"textColor\":\"#FFFFFF\",\"backgroundColor\":\"#000000\",\"primaryButtonColor\":\"#AAAAAA\",\"secondaryButtonColor\":\"#BBBBBB\",\"warningButtonColor\":\"#CCCCCC\",\"dangerButtonColor\":\"#DDDDDD\"}")
        json == expectedJson
    }

    def "Admin should get his company"() {
        when: "a token is requested from Keycloak"
        def token = getAdminAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/admin/config/companyInfo endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(
                "${companyServiceBase}/api/admin/config/companyInfo",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the company details"
        response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        def expectedJson = new JsonSlurper().parseText("{\"id\":1,\"name\":\"Test Company\",\"address\":{\"id\":1,\"address1\":\"123 Main St.\",\"address2\":null,\"city\":\"Marietta\",\"state\":\"Georgia\",\"zipcode\":\"30068\",\"country\":null},\"phoneNumber\":{\"id\":1,\"countryCode\":1,\"phoneNumber\":1231231234},\"logoUrl\":\"logoUrl.com\",\"landingPrompt\":\"What do you want to do?\",\"textColor\":\"#FFFFFF\",\"backgroundColor\":\"#000000\",\"primaryButtonColor\":\"#AAAAAA\",\"secondaryButtonColor\":\"#BBBBBB\",\"warningButtonColor\":\"#CCCCCC\",\"dangerButtonColor\":\"#DDDDDD\"}")
        json == expectedJson
    }

    def "Admin user should generate new QR codes"() {
        when: "a token is requested from Keycloak"
        def token = getAdminAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/admin/config/qrcode endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(
                "${companyServiceBase}/api/admin/config/qrcode?count=1",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the paginated response of companies"
        response.statusCode == HttpStatus.OK
        response.body.size() > 500
    }

    def "Unauthenticated user should get specific company"() {
        when: "the /api/internal/companies/{companyId}/companyInfo endpoint is called"
        def request = new HttpEntity<>()
        def response = restTemplate.exchange(
                "${companyServiceBase}/api/public/companies/1/companyInfo",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the company details"
        response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        def expectedJson = new JsonSlurper().parseText("{\"id\":1,\"name\":\"Test Company\",\"address\":{\"id\":1,\"address1\":\"123 Main St.\",\"address2\":null,\"city\":\"Marietta\",\"state\":\"Georgia\",\"zipcode\":\"30068\",\"country\":null},\"phoneNumber\":{\"id\":1,\"countryCode\":1,\"phoneNumber\":1231231234},\"logoUrl\":\"logoUrl.com\",\"landingPrompt\":\"What do you want to do?\",\"textColor\":\"#FFFFFF\",\"backgroundColor\":\"#000000\",\"primaryButtonColor\":\"#AAAAAA\",\"secondaryButtonColor\":\"#BBBBBB\",\"warningButtonColor\":\"#CCCCCC\",\"dangerButtonColor\":\"#DDDDDD\"}")
        json == expectedJson
    }
}
