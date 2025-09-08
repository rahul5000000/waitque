package com.rrsgroup.company.service;

import com.rrsgroup.company.dto.LeadFlowDto;
import com.rrsgroup.company.dto.LeadFlowQuestionDto;
import com.rrsgroup.company.entity.LeadFlow;
import com.rrsgroup.company.entity.LeadFlowOrder;
import com.rrsgroup.company.entity.LeadFlowQuestion;
import org.springframework.stereotype.Service;

@Service
public class LeadFlowDtoMapper {
    public LeadFlowDto map(LeadFlow leadFlow) {
        return new LeadFlowDto(leadFlow.getId(), leadFlow.getLeadFlowOrder().getCompany().getId(),
                leadFlow.getStatus(), leadFlow.getName(), leadFlow.getIcon(), leadFlow.getButtonText(),
                leadFlow.getTitle(), leadFlow.getConfirmationMessageHeader(), leadFlow.getConfirmationMessage1(),
                leadFlow.getConfirmationMessage2(), leadFlow.getConfirmationMessage3(),
                leadFlow.getLeadFlowOrder().getOrdinal(),
                leadFlow.getQuestions().stream()
                        .map(this::map).toList());
    }

    public LeadFlowQuestionDto map(LeadFlowQuestion question) {
        return new LeadFlowQuestionDto(question.getId(), question.getQuestion(), question.getDataType());
    }

    public LeadFlowQuestion map(LeadFlowQuestionDto dto) {
        return LeadFlowQuestion.builder().id(dto.id()).question(dto.question()).dataType(dto.dataType()).build();
    }

    public LeadFlow map(LeadFlowDto dto) {
        return LeadFlow.builder().id(dto.id()).status(dto.status()).name(dto.name()).icon(dto.iconUrl())
                .buttonText(dto.buttonText()).title(dto.title())
                .confirmationMessageHeader(dto.confirmationMessageHeader())
                .confirmationMessage1(dto.confirmationMessage1())
                .confirmationMessage2(dto.confirmationMessage2())
                .confirmationMessage3(dto.confirmationMessage3())
                .leadFlowOrder(LeadFlowOrder.builder().ordinal(dto.ordinal()).build())
                .questions(dto.questions().stream().map(this::map).toList()).build();
    }
}
