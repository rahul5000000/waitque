package com.rrsgroup.company.service

import spock.lang.Specification

class FrontEndLinkServiceSpec extends Specification {
    def service = new FrontEndLinkService()

    def setup() {
        // Manually inject the value of selfBaseUrl since @Value won't work in unit test without Spring context
        service.appFrontendBaseUrl = "http://localhost:8080"
    }

    def "getCustomerLandingPageLink should return correct URL"() {
        given:
        Long companyId = 123L
        UUID customerCode = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

        when:
        def result = service.getCustomerLandingPageLink(companyId, customerCode)

        then:
        result == "http://localhost:8080?company=123&customerCode=123e4567-e89b-12d3-a456-426614174000"
    }
}
