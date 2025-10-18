package com.rrsgroup.integration

import groovy.json.JsonSlurper
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification

abstract class IntegrationTestSpec extends Specification {
    private String keycloakBase = "http://keycloak:8080"
    private String realm = "rrs-waitque"
    private String clientId = "rrs-waitque-confidential"
    private String clientSecret = "y4IW62E7zuvI8ACWjhkIjNJZta9VPwp9"
    private String superUserUsername = "superuser"
    private String superUserPassword = "test"
    private String adminUsername = "admintest1"
    private String adminPassword = "test"
    private String fieldUsername = "fieldtest1"
    private String fieldPassword = "test"

    private String companyServiceBase = "http://localhost:8082"

    def restTemplate = new RestTemplate()

    String getSuperUserAccessToken() {
        return getAccessToken(superUserUsername, superUserPassword)
    }

    String getAdminAccessToken() {
        return getAccessToken(adminUsername, adminPassword)
    }

    String getFieldUserAccessToken() {
        return getAccessToken(fieldUsername, fieldPassword)
    }

    String getNonSuperUserAccessToken() {
        return ((int) (Math.random() * 2)) % 2 == 0 ? getAdminAccessToken() : getFieldUserAccessToken()
    }

    String getNonAdminAccessToken() {
        return ((int) (Math.random() * 2)) % 2 == 0 ? getSuperUserAccessToken() : getFieldUserAccessToken()
    }

    private String getAccessToken(String username, String password) {
        def url = "${keycloakBase}/realms/${realm}/protocol/openid-connect/token"

        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)

        def body = UriComponentsBuilder.newInstance()
                .queryParam("grant_type", 'password')
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("username", username)
                .queryParam("password", password)
                .build()
                .toUri()
                .rawQuery

        def request = new HttpEntity<>(body, headers)
        def response = restTemplate.exchange(url, HttpMethod.POST, request, String)

        assert response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        return json.access_token
    }

    Long initTestCompany() {
        def token = getSuperUserAccessToken()
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        headers.setContentType(MediaType.APPLICATION_JSON)

        // Does company already exist?
        try {
            def request = new HttpEntity<>(headers)
            def response = restTemplate.exchange(
                    "${companyServiceBase}/api/internal/companies/1",
                    HttpMethod.GET,
                    request,
                    String
            )

            assert response.statusCode == HttpStatus.OK

            def json = new JsonSlurper().parseText(response.body)
            return json.id
        } catch (Exception e) {
            // Swallow exception for non-200 responses so that we can create the company below
        }

        // If not, create it
        def url = "${companyServiceBase}/api/internal/companies"
        def body = "{\"name\":\"Test Company\",\"address\":{\"address1\":\"123 Main St.\",\"city\":\"Marietta\",\"state\":\"Georgia\",\"zipcode\":\"30068\"},\"phoneNumber\":{\"countryCode\":1,\"phoneNumber\":1231231234},\"logoUrl\":\"logoUrl.com\",\"landingPrompt\":\"What do you want to do?\",\"textColor\":\"#FFFFFF\",\"backgroundColor\":\"#000000\",\"primaryButtonColor\":\"#AAAAAA\",\"secondaryButtonColor\":\"#BBBBBB\",\"warningButtonColor\":\"#CCCCCC\",\"dangerButtonColor\":\"#DDDDDD\"}"
        def request = new HttpEntity<>(body, headers)
        def response = restTemplate.exchange(url, HttpMethod.POST, request, String)

        assert response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        return json.id
    }

    Long initLeadFlow() {
        def token = getAdminAccessToken()
        def headers = new HttpHeaders()
        headers.setBearerAuth(token)
        headers.setContentType(MediaType.APPLICATION_JSON)

        // Does lead flow already exist?
        try {
            def request = new HttpEntity<>(headers)
            def response = restTemplate.exchange(
                    "${companyServiceBase}/api/admin/flows/1",
                    HttpMethod.GET,
                    request,
                    String
            )

            response.statusCode == HttpStatus.OK

            def json = new JsonSlurper().parseText(response.body)
            return json.id
        } catch (Exception e) {
            // Swallow exception for non-200 responses so that we can create the lead flow below
        }

        // If not, create it
        def url = "${companyServiceBase}/api/admin/flows"
        def body = "{\"companyId\":1,\"status\":\"ACTIVE\",\"name\":\"Test Lead Flow\",\"iconUrl\":\"test.jpg\",\"buttonText\":\"Schedule\",\"title\":\"Book Test\",\"confirmationMessageHeader\":\"Booked!\",\"confirmationMessage1\":\"We're on our way!\",\"ordinal\":1,\"questions\":[{\"question\":\"Question 1\",\"dataType\":\"BOOLEAN\",\"isRequired\":true},{\"question\":\"Question 2\",\"dataType\":\"TEXT\",\"isRequired\":false}]}\n"
        def request = new HttpEntity<>(body, headers)
        def response = restTemplate.exchange(url, HttpMethod.POST, request, String)

        assert response.statusCode == HttpStatus.OK

        def json = new JsonSlurper().parseText(response.body)
        return json.id
    }
}

