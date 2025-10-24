package com.rrsgroup.integration

import groovy.json.JsonSlurper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

class LeadFlowControllerIT extends IntegrationTestSpec {
    def companyServiceBase = "http://localhost:8082"
    def restTemplate = new RestTemplate()

    def setup() {
        initTestCompany()
        initLeadFlow()
    }

    def "Admin user should get list of lead flows"() {
        when: "a token is requested from Keycloak"
        def token = getAdminAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/admin/flows endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(
                "${companyServiceBase}/api/admin/flows?status=ACTIVE&limit=10&page=0&sortDir=DESC&sortField=ordinal",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the paginated response of companies"
        response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        def expectedJson = new JsonSlurper().parseText("{\"page\":0,\"limit\":10,\"total\":1,\"sortField\":\"ordinal\",\"sortDir\":\"DESC\",\"leadFlows\":[{\"id\":1,\"name\":\"Test Lead Flow\",\"status\":\"ACTIVE\",\"ordinal\":1}]}")
        json == expectedJson
    }

    def "Non-Admin user should get a 403 when getting list of lead flows"() {
        when: "a token is requested from Keycloak"
        def token = getNonAdminAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/admin/flows endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def exception

        try {
            def response = restTemplate.exchange(
                    "${companyServiceBase}/api/admin/flows?status=ACTIVE&limit=10&page=0&sortDir=DESC&sortField=ordinal",
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

    def "Admin user should get specific lead flows"() {
        when: "a token is requested from Keycloak"
        def token = getAdminAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/admin/flows/{leadFlowId} endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(
                "${companyServiceBase}/api/admin/flows/1",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the paginated response of companies"
        response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        def expectedJson = new JsonSlurper().parseText("{\"id\":1,\"companyId\":1,\"status\":\"ACTIVE\",\"name\":\"Test Lead Flow\",\"icon\":\"test-icon\",\"buttonText\":\"Schedule\",\"title\":\"Book Test\",\"confirmationMessageHeader\":\"Booked!\",\"confirmationMessage1\":\"We're on our way!\",\"confirmationMessage2\":null,\"confirmationMessage3\":null,\"ordinal\":1,\"questions\":[{\"id\":1,\"question\":\"Question 1\",\"dataType\":\"BOOLEAN\",\"isRequired\":true},{\"id\":2,\"question\":\"Question 2\",\"dataType\":\"TEXT\",\"isRequired\":false}],\"predecessorId\":null}")
        json == expectedJson
    }

    def "Non-Admin user should get a 403 when trying to get a specific lead flows"() {
        when: "a token is requested from Keycloak"
        def token = getNonAdminAccessToken()

        then: "token is received"
        token
        token.size() > 100

        when: "the /api/admin/flows/{leadFlowId} endpoint is called"
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        def request = new HttpEntity<>(headers)
        def exception

        try {
            def response = restTemplate.exchange(
                    "${companyServiceBase}/api/admin/flows/1",
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

    def "Unauthenticated user should get specific company's lead flow list"() {
        when: "the /api/public/companies/{companyId}/flows endpoint is called"
        def request = new HttpEntity<>()
        def response = restTemplate.exchange(
                "${companyServiceBase}/api/public/companies/1/flows?limit=10&page=0",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the lead flows"
        response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        def expectedJson = new JsonSlurper().parseText("{\"page\":0,\"limit\":10,\"total\":1,\"sortField\":\"ordinal\",\"sortDir\":\"ASC\",\"leadFlows\":[{\"id\":1,\"name\":\"Test Lead Flow\",\"ordinal\":1,\"icon\":\"test.jpg\"}]}")
        json == expectedJson
    }

    def "Unauthenticated user should get specific company's lead flow details"() {
        when: "the /api/public/companies/{companyId}/flows/{leadFlowId} endpoint is called"
        def request = new HttpEntity<>()
        def response = restTemplate.exchange(
                "${companyServiceBase}/api/public/companies/1/flows/1",
                HttpMethod.GET,
                request,
                String
        )

        then: "the response is OK and contains the lead flows"
        response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        def expectedJson = new JsonSlurper().parseText("{\"id\":1,\"companyId\":1,\"status\":\"ACTIVE\",\"name\":\"Test Lead Flow\",\"icon\":\"test-icon\",\"buttonText\":\"Schedule\",\"title\":\"Book Test\",\"confirmationMessageHeader\":\"Booked!\",\"confirmationMessage1\":\"We're on our way!\",\"confirmationMessage2\":null,\"confirmationMessage3\":null,\"ordinal\":1,\"questions\":[{\"id\":1,\"question\":\"Question 1\",\"dataType\":\"BOOLEAN\",\"isRequired\":true},{\"id\":2,\"question\":\"Question 2\",\"dataType\":\"TEXT\",\"isRequired\":false}],\"predecessorId\":null}")
        json == expectedJson
    }
}
