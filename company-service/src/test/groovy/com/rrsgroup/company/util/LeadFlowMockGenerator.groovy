package com.rrsgroup.company.util

import com.rrsgroup.company.domain.LeadFlowQuestionDataType
import com.rrsgroup.company.domain.LeadFlowStatus
import com.rrsgroup.company.dto.LeadFlowBooleanQuestionDto
import com.rrsgroup.company.dto.LeadFlowDto
import com.rrsgroup.company.dto.LeadFlowQuestionAnswerDto
import com.rrsgroup.company.dto.LeadFlowQuestionDto
import com.rrsgroup.company.entity.Company
import com.rrsgroup.company.entity.LeadFlow
import com.rrsgroup.company.entity.LeadFlowOrder
import com.rrsgroup.company.entity.LeadFlowQuestion
import spock.lang.Specification

class LeadFlowMockGenerator extends Specification {
    LeadFlowDto getLeadFlowDtoMock(Long id, Long companyId) {
        return getLeadFlowDtoMock(id, companyId, null)
    }

    LeadFlowDto getLeadFlowDtoMock(Long id, Long companyId, Long predecessorId) {
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
        def question1IsRequired = true
        def question2Id = 4L
        def question2 = "question2"
        def question2DataType = LeadFlowQuestionDataType.TEXT
        def question2IsRequired = false

        List<LeadFlowQuestionDto> questionDtos = new ArrayList<>()
        questionDtos.add(new LeadFlowBooleanQuestionDto(question1Id, question1, question1DataType, question1IsRequired, "No", "Yes"))
        questionDtos.add(new LeadFlowQuestionAnswerDto(question2Id, question2, question2DataType, question2IsRequired))

        def dto = new LeadFlowDto(id, companyId, status, name, iconUrl, buttonText, title, confirmationMessageHeader,
                confirmationMessage1, confirmationMessage2, confirmationMessage3, ordinal, questionDtos, predecessorId)

        return dto
    }

    LeadFlow getLeadFlowMock(Long id, Long companyId) {
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

        return leadFlow
    }
}
