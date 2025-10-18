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

    def restTemplate = new RestTemplate()

    public String getSuperUserAccessToken() {
        return getAccessToken(superUserUsername, superUserPassword)
    }

    public String getAdminAccessToken() {
        return getAccessToken(adminUsername, adminPassword)
    }

    public String getFieldUserAccessToken() {
        return getAccessToken(fieldUsername, fieldPassword)
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
}

