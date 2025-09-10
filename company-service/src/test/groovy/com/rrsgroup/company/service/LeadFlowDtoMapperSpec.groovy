package com.rrsgroup.company.service

import com.rrsgroup.company.domain.LeadFlowQuestionDataType
import com.rrsgroup.company.domain.Status
import com.rrsgroup.company.dto.LeadFlowDto
import com.rrsgroup.company.dto.LeadFlowQuestionDto
import com.rrsgroup.company.entity.Company
import com.rrsgroup.company.entity.LeadFlow
import com.rrsgroup.company.entity.LeadFlowOrder
import com.rrsgroup.company.entity.LeadFlowQuestion
import spock.lang.Specification

class LeadFlowDtoMapperSpec extends Specification {
    def leadFlowDtoMapper = new LeadFlowDtoMapper()

    def "can map from LeadFlow to LeadFlowDto"() {
        given:
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

        when:
        def result = leadFlowDtoMapper.map(leadFlow)

        then:
        result.id() == id
        result.companyId() == companyId
        result.status() == status
        result.name() == name
        result.iconUrl() == iconUrl
        result.buttonText() == buttonText
        result.title() == title
        result.confirmationMessageHeader() == confirmationMessageHeader
        result.confirmationMessage1() == confirmationMessage1
        result.confirmationMessage2() == confirmationMessage2
        result.confirmationMessage3() == confirmationMessage3
        result.ordinal() == ordinal
        result.questions().get(0).id() == question1Id
        result.questions().get(0).question() == question1
        result.questions().get(0).dataType() == question1DataType
        result.questions().get(1).id() == question2Id
        result.questions().get(1).question() == question2
        result.questions().get(1).dataType() == question2DataType
    }

    def "can map from LeadFlowDto to LeadFlow"() {
        given:
        def id = 1L
        def companyId = 2L
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

        List<LeadFlowQuestionDto> questionDtos = new ArrayList<>()
        questionDtos.add(new LeadFlowQuestionDto(question1Id, question1, question1DataType))
        questionDtos.add(new LeadFlowQuestionDto(question2Id, question2, question2DataType))

        def dto = new LeadFlowDto(id, companyId, status, name, iconUrl, buttonText, title, confirmationMessageHeader,
                confirmationMessage1, confirmationMessage2, confirmationMessage3, ordinal, questionDtos)

        when:
        def result = leadFlowDtoMapper.map(dto)

        then:
        result.getId() == id
        result.getName() == name
        result.getIcon() == iconUrl
        result.getButtonText() == buttonText
        result.getTitle() == title
        result.getConfirmationMessageHeader() == confirmationMessageHeader
        result.getConfirmationMessage1() == confirmationMessage1
        result.getConfirmationMessage2() == confirmationMessage2
        result.getConfirmationMessage3() == confirmationMessage3
        result.getLeadFlowOrder().getOrdinal() == ordinal
        result.getLeadFlowOrder().getLeadFlow() == result
        result.getLeadFlowOrder().getStatus() == status
        result.getQuestions().get(0).getId() == question1Id
        result.getQuestions().get(0).getQuestion() == question1
        result.getQuestions().get(0).getDataType() == question1DataType
        result.getQuestions().get(0).getLeadFlow() == result
        result.getQuestions().get(1).getId() == question2Id
        result.getQuestions().get(1).getQuestion() == question2
        result.getQuestions().get(1).getDataType() == question2DataType
        result.getQuestions().get(1).getLeadFlow() == result
    }
}
