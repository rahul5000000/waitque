package com.rrsgroup.company.web

import com.rrsgroup.common.dto.AdminUserDto
import com.rrsgroup.common.exception.IllegalRequestException
import com.rrsgroup.common.exception.IllegalUpdateException
import com.rrsgroup.common.exception.RecordNotFoundException
import com.rrsgroup.company.service.LeadFlowDtoMapper
import com.rrsgroup.company.service.LeadFlowService
import com.rrsgroup.company.util.LeadFlowMockGenerator
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
        def e = thrown(IllegalRequestException.class)
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
        def e = thrown(IllegalUpdateException.class)
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

    def "getLeadFlow returns leadDto when lead is found for logged in user's company"() {
        given:
        def userId = "abcd"
        def companyId = 1L
        def user = Mock(AdminUserDto)
        user.getUserId() >> userId
        def leadFlowId = 3L
        def leadFlow = leadFlowMockGenerator.getLeadFlowMock(leadFlowId, companyId)

        when:
        def result = leadFlowController.getLeadFlow(user, leadFlowId)

        then:
        1 * user.getCompanyId() >> companyId
        1 * leadFlowService.getLeadFlow(leadFlowId, companyId) >> leadFlow
        0 * _
        result.id() == leadFlowId
    }

    def "getLeadFlow throws RecordNotFoundException exception if lead is not found"() {
        given:
        def userId = "abcd"
        def companyId = 1L
        def user = Mock(AdminUserDto)
        user.getUserId() >> userId
        def leadFlowId = 3L

        when:
        leadFlowController.getLeadFlow(user, leadFlowId)

        then:
        1 * user.getCompanyId() >> companyId
        1 * leadFlowService.getLeadFlow(leadFlowId, companyId) >> null
        0 * _
        thrown(RecordNotFoundException.class)
    }

    def "updateLeadFlow throws IllegalRequestException if the leadFlowId in the request and URL do not match"() {
        given:
        def userId = "abcd"
        def companyId = 1L
        def user = Mock(AdminUserDto)
        user.getUserId() >> userId
        def leadFlowId = 3L
        def leadFlowDto = leadFlowMockGenerator.getLeadFlowDtoMock(leadFlowId + 1, companyId)

        when:
        leadFlowController.updateLeadFlow(user, leadFlowId, leadFlowDto)

        then:
        thrown(IllegalRequestException.class)
    }

    def "updateLeadFlow returns updated LeadFlowDto"() {
        given:
        def userId = "abcd"
        def companyId = 1L
        def user = Mock(AdminUserDto)
        user.getUserId() >> userId
        def leadFlowId = 3L
        def leadFlowDto = leadFlowMockGenerator.getLeadFlowDtoMock(leadFlowId , companyId)
        def leadFlow = leadFlowMockGenerator.getLeadFlowMock(leadFlowId + 1, companyId)

        when:
        def result = leadFlowController.updateLeadFlow(user, leadFlowId, leadFlowDto)

        then:
        1 * leadFlowService.updateLeadFlow(_, _, _) >> leadFlow
        result.id() == leadFlowId + 1
    }

    def "inactivateLeadFlow invokes service to inactivate lead flow"() {
        given:
        def userId = "abcd"
        def companyId = 1L
        def leadFlowId = 3L
        def user = Mock(AdminUserDto) {
            getUserId() >> userId
            getCompanyId() >> companyId
        }
        def leadFlow = leadFlowMockGenerator.getLeadFlowMock(leadFlowId, companyId)

        when:
        leadFlowController.inactivateLeadFlow(user, leadFlowId)

        then:
        1 * leadFlowService.inactivateLeadFlow(leadFlowId, companyId, user) >> leadFlow
    }
}
