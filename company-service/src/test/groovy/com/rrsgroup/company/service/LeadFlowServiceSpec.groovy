package com.rrsgroup.company.service

import com.rrsgroup.common.dto.UserDto
import com.rrsgroup.company.domain.LeadFlowQuestionDataType
import com.rrsgroup.company.domain.Status
import com.rrsgroup.company.entity.Company
import com.rrsgroup.company.entity.LeadFlow
import com.rrsgroup.company.entity.LeadFlowOrder
import com.rrsgroup.company.entity.LeadFlowQuestion
import com.rrsgroup.company.repository.LeadFlowRepository
import spock.lang.Specification

import java.time.LocalDateTime

class LeadFlowServiceSpec extends Specification {
    def leadFlowRepository = Mock(LeadFlowRepository)
    def companyService = Mock(CompanyService)

    def leadFlowService = new LeadFlowService(leadFlowRepository, companyService)

    def "getCompany returns company if the company exists by company ID"() {
        given:
        def company = Mock(Company)
        def companyId = 1L

        when:
        def result = leadFlowService.getCompany(companyId)

        then:
        1 * companyService.getCompany(companyId) >> Optional.of(company)
        0 * _
        result != null
    }

    def "getCompany throws IllegalStateException if company does not exist by company ID"() {
        given:
        def companyId = 1L

        when:
        leadFlowService.getCompany(companyId)

        then:
        1 * companyService.getCompany(companyId) >> Optional.empty()
        0 * _
        thrown(IllegalStateException)
    }

    def "createLeadFlow populates audit fields before saving"() {
        given:
        LocalDateTime startOfTest = LocalDateTime.now()
        def id = 1L
        def companyId = 2L
        def company = Mock(Company)
        company.getId() >> companyId
        def status = Status.INACTIVE
        def name = "name"
        def iconUrl = "test.jpg"
        def buttonText = "Schedule"
        def title = "Book Test"
        def confirmationMessageHeader = "confirmationMessageHeader"
        def confirmationMessage1 = "confirmationMessage1"
        def confirmationMessage2 = "confirmationMessage2"
        def confirmationMessage3 = "confirmationMessage3"
        def ordinal = 0
        def question1Id = 3L
        def question1 = "question1"
        def question1DataType = LeadFlowQuestionDataType.BOOLEAN
        def question2Id = 4L
        def question2 = "question2"
        def question2DataType = LeadFlowQuestionDataType.TEXT

        List<LeadFlowQuestion> questions = new ArrayList<>()
        questions.add(LeadFlowQuestion.builder().id(question1Id).question(question1).dataType(question1DataType).build())
        questions.add(LeadFlowQuestion.builder().id(question2Id).question(question2).dataType(question2DataType).build())

        def leadFlowOrder = LeadFlowOrder.builder().ordinal(ordinal).status(status).company(company).build();

        def leadFlow = LeadFlow.builder().id(id).name(name).icon(iconUrl)
                .buttonText(buttonText).title(title).confirmationMessageHeader(confirmationMessageHeader)
                .confirmationMessage1(confirmationMessage1).confirmationMessage2(confirmationMessage2)
                .confirmationMessage3(confirmationMessage3).leadFlowOrder(leadFlowOrder).questions(questions).build()

        def userId = "abcd"
        def user = Mock(UserDto)

        when:
        def result = leadFlowService.createLeadFlow(leadFlow, companyId, user)

        then:
        1 * companyService.getCompany(companyId) >> Optional.of(company)
        1 * leadFlowRepository.save(leadFlow) >> leadFlow
        1 * user.getUserId() >> userId
        0 * _
        result.getCreatedBy() == userId
        result.getUpdatedBy() == userId
        result.getCreatedDate().isAfter(startOfTest)
        result.getUpdatedDate().isAfter(startOfTest)
        result.getLeadFlowOrder().getCreatedBy() == userId
        result.getLeadFlowOrder().getUpdatedBy() == userId
        result.getLeadFlowOrder().getCreatedDate().isAfter(startOfTest)
        result.getLeadFlowOrder().getUpdatedDate().isAfter(startOfTest)
        result.getQuestions().get(0).getCreatedBy() == userId
        result.getQuestions().get(0).getUpdatedBy() == userId
        result.getQuestions().get(0).getCreatedDate().isAfter(startOfTest)
        result.getQuestions().get(0).getUpdatedDate().isAfter(startOfTest)
        result.getQuestions().get(1).getCreatedBy() == userId
        result.getQuestions().get(1).getUpdatedBy() == userId
        result.getQuestions().get(1).getCreatedDate().isAfter(startOfTest)
        result.getQuestions().get(1).getUpdatedDate().isAfter(startOfTest)
    }
}
