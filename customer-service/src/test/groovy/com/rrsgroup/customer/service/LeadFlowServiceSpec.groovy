package com.rrsgroup.customer.service

import com.rrsgroup.common.exception.RecordNotFoundException
import com.rrsgroup.customer.dto.LeadFlowDto
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.core.ParameterizedTypeReference
import spock.lang.Specification

class LeadFlowServiceSpec extends Specification {

    def webClient = Mock(WebClient)
    def requestHeadersUriSpec = Mock(WebClient.RequestHeadersUriSpec)
    def requestHeadersSpec = Mock(WebClient.RequestHeadersSpec)
    def responseSpec = Mock(WebClient.ResponseSpec)

    def service = new LeadFlowService(webClient)

    def setup() {
        // simulate injected value
        service.companyServiceBaseUrl = "http://company-service"
    }

    def "should return LeadFlowDto when request succeeds"() {
        given:
        Long leadFlowId = 10L
        Long companyId = 99L
        def expected = new LeadFlowDto(leadFlowId, null, null, null, null, null, null, null, null, null, null, null, null, null)

        when:
        def result = service.getLeadFlow(leadFlowId, companyId)

        then:
        1 * webClient.get() >> requestHeadersUriSpec
        1 * requestHeadersUriSpec.uri("http://company-service/api/system/companies/99/flows/10") >> requestHeadersSpec
        1 * requestHeadersSpec.retrieve() >> responseSpec
        1 * responseSpec.bodyToMono(_ as ParameterizedTypeReference<LeadFlowDto>) >> Mock(reactor.core.publisher.Mono) {
            block() >> expected
        }

        result == expected
    }

    def "should throw RecordNotFoundException when 404 returned"() {
        given:
        Long leadFlowId = 1L
        Long companyId = 5L

        when:
        service.getLeadFlow(leadFlowId, companyId)

        then:
        1 * webClient.get() >> requestHeadersUriSpec
        1 * requestHeadersUriSpec.uri("http://company-service/api/system/companies/5/flows/1") >> requestHeadersSpec
        1 * requestHeadersSpec.retrieve() >> responseSpec
        1 * responseSpec.bodyToMono(_ as ParameterizedTypeReference<LeadFlowDto>) >> {
            throw new WebClientResponseException.NotFound("not found", null, null, null, null)
        }

        def ex = thrown(RecordNotFoundException)
        ex.message == "Lead flow does not exist by leadFlowId=1, companyId=5"
    }
}
