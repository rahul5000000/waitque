package com.rrsgroup.customer.web

import com.rrsgroup.common.exception.IllegalRequestException
import com.rrsgroup.common.exception.IllegalUpdateException
import com.rrsgroup.common.exception.RecordNotFoundException
import com.rrsgroup.common.service.S3Service
import com.rrsgroup.customer.domain.LeadFlowQuestionDataType
import com.rrsgroup.customer.domain.LeadFlowStatus
import com.rrsgroup.customer.domain.lead.LeadStatus
import com.rrsgroup.customer.dto.*
import com.rrsgroup.customer.dto.lead.LeadAnswerDto
import com.rrsgroup.customer.dto.lead.LeadBooleanAnswerDto
import com.rrsgroup.customer.dto.lead.LeadDto
import com.rrsgroup.customer.dto.lead.LeadTextAnswerDto
import com.rrsgroup.customer.entity.Customer
import com.rrsgroup.customer.entity.QrCode
import com.rrsgroup.customer.service.*
import com.rrsgroup.customer.service.lead.LeadDtoMapper
import com.rrsgroup.customer.service.lead.LeadService
import spock.lang.Specification

class LeadControllerSpec extends Specification {
    LeadFlowService leadFlowService = Mock()
    CustomerService customerService = Mock()
    LeadDtoMapper leadDtoMapper = Mock()
    LeadService leadService = Mock()
    S3Service s3Service = Mock()
    UploadUrlDtoMapper uploadUrlDtoMapper = Mock()

    LeadController controller = new LeadController(leadFlowService, customerService, leadDtoMapper, leadService, s3Service, uploadUrlDtoMapper)

    def "validateRequiredQuestionsAreAnswered throws when required question missing"() {
        given:
        def leadFlow = generateLeadFlow()
        def answer = new LeadTextAnswerDto(2L, 2L, "Some answer")
        def leadDto = new LeadDto(1L, 1L, LeadStatus.NEW, "Alice", "Smith", null, null, "alice@test.com", [answer], null, null, null, null, null, null, null)

        when:
        controller."validateRequiredQuestionsAreAnswered"(leadDto, leadFlow)

        then:
        def ex = thrown(IllegalRequestException)
        ex.message.contains("required but not provided")
    }

    def "validateRequiredQuestionsAreAnswered passes when all required answered"() {
        given:
        def leadFlow = generateLeadFlow()
        def answer = new LeadBooleanAnswerDto(1L, 1L, true)
        def leadDto = new LeadDto(1L, 1L, LeadStatus.NEW, "Alice", "Smith", null, null, "alice@test.com", [answer], null, null, null, null, null, null, null)

        when:
        controller."validateRequiredQuestionsAreAnswered"(leadDto, leadFlow)

        then:
        noExceptionThrown()
    }

    def "validateAnswersMatchQuestions throws when answer questionId not in leadFlow"() {
        given:
        def leadFlow = generateLeadFlow()
        def answer = new LeadTextAnswerDto(3L, 3L, "Some answer")
        def leadDto = new LeadDto(1L, 1L, LeadStatus.NEW, "Alice", "Smith", null, null, "alice@test.com", [answer], null, null, null, null, null, null, null)

        when:
        controller."validateAnswersMatchQuestions"(leadDto, leadFlow)

        then:
        def ex = thrown(IllegalRequestException)
        ex.message.contains("do not have a matching question")
    }

    def "validateAnswersMatchQuestions throws when data type mismatched"() {
        given:
        def leadFlow = generateLeadFlow()
        def answer = new LeadTextAnswerDto(2L, 1L, "Some answer")
        def leadDto = new LeadDto(1L, 1L, LeadStatus.NEW, "Alice", "Smith", null, null, "alice@test.com", [answer], null, null, null, null, null, null, null)

        when:
        controller."validateAnswersMatchQuestions"(leadDto, leadFlow)

        then:
        def ex = thrown(IllegalRequestException)
        ex.message.contains("wrong type")
    }

    def "validateAnswersMatchQuestions passes when ids and datatypes correct"() {
        given:
        def leadFlow = generateLeadFlow()
        def answerBoolean = new LeadBooleanAnswerDto(2L, 1L, true)
        def answerText = new LeadTextAnswerDto(2L, 2L, "Some answer")
        def leadDto = new LeadDto(1L, 1L, LeadStatus.NEW, "Alice", "Smith", null, null, "alice@test.com", [answerBoolean, answerText], null, null, null, null, null, null, null)

        when:
        controller."validateAnswersMatchQuestions"(leadDto, leadFlow)

        then:
        noExceptionThrown()
    }

    def "getActiveLeadFlow throws when leadFlow not found"() {
        given:
        leadFlowService.getLeadFlow(1L, 1L) >> Optional.empty()

        when:
        controller."getActiveLeadFlow"(1L, 1L)

        then:
        thrown(RecordNotFoundException)
    }

    def "getActiveLeadFlow throws when leadFlow inactive"() {
        given:
        def leadFlow = generateLeadFlow(LeadFlowStatus.INACTIVE)
        leadFlowService.getLeadFlow(1L, 1L) >> Optional.of(leadFlow)

        when:
        controller."getActiveLeadFlow"(1L, 1L)

        then:
        thrown(IllegalUpdateException)
    }

    def "getActiveLeadFlow returns active leadFlow"() {
        given:
        def leadFlow = generateLeadFlow(LeadFlowStatus.ACTIVE)
        leadFlowService.getLeadFlow(1L, 1L) >> Optional.of(leadFlow)

        when:
        def result = controller."getActiveLeadFlow"(1L, 1L)

        then:
        result == leadFlow
    }

    private LeadFlowDto generateLeadFlow() {
        return generateLeadFlow(LeadFlowStatus.ACTIVE)
    }

    private LeadFlowDto generateLeadFlow(LeadFlowStatus status) {
        def id = 1L
        def companyId = 2L
        def name = "name"
        def iconUrl = "test.jpg"
        def buttonText = "Schedule"
        def title = "Book Test"
        def confirmationMessageHeader = "confirmationMessageHeader"
        def confirmationMessage1 = "confirmationMessage1"
        def confirmationMessage2 = "confirmationMessage2"
        def confirmationMessage3 = "confirmationMessage3"
        def ordinal = 0
        def question1Id = 1L
        def question1 = "question1"
        def question1DataType = LeadFlowQuestionDataType.BOOLEAN
        def question1IsRequired = true
        def question2Id = 2L
        def question2 = "question2"
        def question2DataType = LeadFlowQuestionDataType.TEXT
        def question2IsRequired = false

        List<LeadFlowQuestionDto> questionDtos = new ArrayList<>()
        questionDtos.add(new LeadFlowBooleanQuestionDto(question1Id, question1, question1DataType, question1IsRequired, "No", "Yes"))
        questionDtos.add(new LeadFlowQuestionAnswerDto(question2Id, question2, question2DataType, question2IsRequired))

        return new LeadFlowDto(id, companyId, status, name, iconUrl, buttonText, title, confirmationMessageHeader,
                confirmationMessage1, confirmationMessage2, confirmationMessage3, ordinal, questionDtos, null)
    }
}
