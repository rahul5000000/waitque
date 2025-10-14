package com.rrsgroup.company.service

import com.rrsgroup.common.dto.UserDto
import com.rrsgroup.common.exception.IllegalUpdateException
import com.rrsgroup.common.exception.RecordNotFoundException
import com.rrsgroup.company.domain.LeadFlowQuestionDataType
import com.rrsgroup.company.domain.LeadFlowStatus
import com.rrsgroup.company.entity.Company
import com.rrsgroup.company.entity.LeadFlow
import com.rrsgroup.company.entity.LeadFlowOrder
import com.rrsgroup.company.entity.LeadFlowQuestion
import com.rrsgroup.company.repository.LeadFlowRepository
import org.springframework.dao.DataIntegrityViolationException
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
        def status = LeadFlowStatus.INACTIVE
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

    def "createLeadFlow throws IllegalUpdateException for unique key constraint violation"() {
        def companyId = 2L
        def company = Mock(Company) {
            getId() >> { -> companyId }
        }
        def leadFlow = Mock(LeadFlow) {
            getLeadFlowOrder() >> { -> Mock(LeadFlowOrder)}
            getQuestions() >> { -> new ArrayList<LeadFlowQuestion>()}
        }
        def user = Mock(UserDto)

        when:
        leadFlowService.createLeadFlow(leadFlow, companyId, user)

        then:
        user.getUserId() >> "abcd"
        companyService.getCompany(companyId) >> Optional.of(company)
        leadFlowRepository.save(leadFlow) >> {throw new DataIntegrityViolationException("uq_lead_flow_order_company_id_ordinal_status_active")}
        thrown(IllegalUpdateException.class)
    }

    def "createLeadFlow rethrows DataIntegrityViolationException for violations other than unique key constraint violation"() {
        def companyId = 2L
        def company = Mock(Company) {
            getId() >> { -> companyId }
        }
        def leadFlow = Mock(LeadFlow) {
            getLeadFlowOrder() >> { -> Mock(LeadFlowOrder)}
            getQuestions() >> { -> new ArrayList<LeadFlowQuestion>()}
        }
        def user = Mock(UserDto)

        when:
        leadFlowService.createLeadFlow(leadFlow, companyId, user)

        then:
        user.getUserId() >> "abcd"
        companyService.getCompany(companyId) >> Optional.of(company)
        leadFlowRepository.save(leadFlow) >> {throw new DataIntegrityViolationException("some_other_constraint")}
        thrown(DataIntegrityViolationException.class)
    }

    def "getLeadFlow invokes repository to get lead flow by ID and companyId"() {
        given:
        def companyId = 2L
        def leadFlowId = 3L
        def leadFlow = Mock(LeadFlow) {
            getId() >> { -> leadFlowId}
        }

        when:
        def result = leadFlowService.getLeadFlow(leadFlowId, companyId)

        then:
        1 * leadFlowRepository.findByIdAndCompanyId(leadFlowId, companyId) >> leadFlow
        result.getId() == leadFlowId
    }

    def "updateLeadFlow throws RecordNotFoundException if existing lead does not exist"() {
        given:
        def companyId = 1L
        def user = Mock(UserDto) {
            getUserId() >> { -> "abcd" }
        }
        def leadFlow = Mock(LeadFlow) {
            getId() >> { -> null }
        }

        when:
        leadFlowService.updateLeadFlow(leadFlow, companyId, user)

        then:
        thrown(RecordNotFoundException.class)
    }

    def "updateLeadFlow creates a new LeadFlow for update with audit fields copied from original LeadFlow"() {
        given:
        def companyId = 1L
        def company = Mock(Company) {
            getId() >> { -> companyId }
        }
        def user = Mock(UserDto) {
            getUserId() >> { -> "updatedBy" }
        }
        def leadFlowQuestion1 = Mock(LeadFlowQuestion)
        def leadFlowQuestion2 = Mock(LeadFlowQuestion)
        def leadFlowOrder = Mock(LeadFlowOrder)
        def leadFlowId = 2L
        def leadFlow = Mock(LeadFlow) {
            getId() >> { -> leadFlowId }
            getQuestions() >> { -> List.of(leadFlowQuestion1, leadFlowQuestion2) }
            getLeadFlowOrder() >> { -> leadFlowOrder }
        }
        def createdDate = LocalDateTime.now().minusDays(1)
        def createdBy = "createdBy"
        def existingLeadFlowOrder = Mock(LeadFlowOrder)
        def existingLeadFlow = Mock(LeadFlow) {
            getCreatedDate() >> { -> createdDate}
            getCreatedBy() >> { -> createdBy }
            getLeadFlowOrder() >> { -> existingLeadFlowOrder }
        }

        when:
        leadFlowService.updateLeadFlow(leadFlow, companyId, user)

        then:
        1 * leadFlowRepository.findByIdAndCompanyId(leadFlowId, companyId) >> existingLeadFlow
        1 * leadFlowRepository.saveAndFlush(existingLeadFlow)
        1 * leadFlowRepository.save(leadFlow)
        1 * companyService.getCompany(companyId) >> Optional.of(company)
    }

    def "inactivateLeadFlow should set status to INACTIVE and update metadata"() {
        given:
        Long leadFlowId = 1L
        Long companyId = 10L
        def user = Mock(UserDto) {
            getUserId() >> { -> "updatedBy" }
        }
        def existingLeadFlow = new LeadFlow(
                id: leadFlowId,
                leadFlowOrder: new LeadFlowOrder(status: LeadFlowStatus.ACTIVE)
        )

        when:
        def result = leadFlowService.inactivateLeadFlow(leadFlowId, companyId, user)

        then:
        1 * leadFlowRepository.findByIdAndCompanyId(leadFlowId, companyId) >> existingLeadFlow
        1 * leadFlowRepository.saveAndFlush(_ as LeadFlow) >> { LeadFlow lf ->
            assert lf.leadFlowOrder.status == LeadFlowStatus.INACTIVE
            assert lf.updatedBy == user.userId
            assert lf.updatedDate != null
            return lf
        }

        result.leadFlowOrder.status == LeadFlowStatus.INACTIVE
        result.updatedBy == user.userId
        result.updatedDate instanceof LocalDateTime
    }

    def "inactivateLeadFlow should throw RecordNotFoundException if leadFlow does not exist"() {
        given:
        Long leadFlowId = 999L
        Long companyId = 10L
        def user = Mock(UserDto) {
            getUserId() >> { -> "updatedBy" }
        }

        when:
        leadFlowService.inactivateLeadFlow(leadFlowId, companyId, user)

        then:
        1 * leadFlowRepository.findByIdAndCompanyId(leadFlowId, companyId) >> null
        thrown(RecordNotFoundException)
        0 * leadFlowRepository.saveAndFlush(_)
    }
}
