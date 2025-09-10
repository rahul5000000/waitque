package com.rrsgroup.company.web

import com.rrsgroup.common.dto.AdminUserDto
import com.rrsgroup.company.service.LeadFlowDtoMapper
import com.rrsgroup.company.service.LeadFlowService
import com.rrsgroup.company.util.LeadFlowMockGenerator
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class LeadFlowControllerSpec extends Specification {
    def leadFlowMockGenerator = new LeadFlowMockGenerator()
    def leadFlowDtoMapper = new LeadFlowDtoMapper()
    def leadFlowService = Mock(LeadFlowService)
    def leadFlowController = new LeadFlowController(leadFlowDtoMapper, leadFlowService)

    def "addLeadFlow throws a BAD REQUEST exception for requests that contain IDs"() {
        given:
        def leadFlowDto = leadFlowMockGenerator.getLeadFlowDtoMock(1L, 2L)
        def userId = "abcd"
        def user = Mock(AdminUserDto)
        user.getUserId() >> userId

        when:
        leadFlowController.addLeadFlow(user, leadFlowDto)

        then:
        def e = thrown(ResponseStatusException.class)
        e.getStatusCode() == HttpStatus.BAD_REQUEST
    }

    def "addLeadFlow throws a CONFLICT exception for requests that contain company ID, but does not match user's company"() {
        given:
        def leadFlowDto = leadFlowMockGenerator.getLeadFlowDtoMock(null, 2L)
        def userId = "abcd"
        def companyId = 1L
        def user = Mock(AdminUserDto)
        user.getUserId() >> userId
        user.getCompanyId() >> companyId

        when:
        leadFlowController.addLeadFlow(user, leadFlowDto)

        then:
        def e = thrown(ResponseStatusException.class)
        e.getStatusCode() == HttpStatus.CONFLICT
    }

    def "addLeadFlow creates lead if request does not contain companyId"() {
        given:
        def leadFlowDto = leadFlowMockGenerator.getLeadFlowDtoMock(null, null)
        def userId = "abcd"
        def companyId = 1L
        def user = Mock(AdminUserDto)
        user.getUserId() >> userId

        when:
        leadFlowController.addLeadFlow(user, leadFlowDto)

        then:
        1 * leadFlowService.createLeadFlow(_, companyId, user) >> leadFlowMockGenerator.getLeadFlowMock(1L, companyId)
        1 * user.getCompanyId() >> companyId
        0 * _
    }

    def "addLeadFlow creates lead if request contains companyId that matches user's company Id"() {
        given:
        def companyId = 1L
        def leadFlowDto = leadFlowMockGenerator.getLeadFlowDtoMock(null, companyId)
        def userId = "abcd"
        def user = Mock(AdminUserDto)
        user.getUserId() >> userId

        when:
        leadFlowController.addLeadFlow(user, leadFlowDto)

        then:
        1 * leadFlowService.createLeadFlow(_, companyId, user) >> leadFlowMockGenerator.getLeadFlowMock(1L, companyId)
        1 * user.getCompanyId() >> companyId
        0 * _
    }
}
