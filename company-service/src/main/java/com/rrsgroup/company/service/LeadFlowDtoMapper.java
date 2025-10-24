package com.rrsgroup.company.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.util.PageableWrapper;
import com.rrsgroup.company.domain.LeadFlowQuestionDataType;
import com.rrsgroup.company.dto.*;
import com.rrsgroup.company.entity.LeadFlow;
import com.rrsgroup.company.entity.LeadFlowOrder;
import com.rrsgroup.company.entity.LeadFlowQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadFlowDtoMapper {
    public LeadFlowDto map(LeadFlow leadFlow) {
        Long predecessorId = null;
        LeadFlow predecessor = leadFlow.getPredecessor();

        if(predecessor != null) {
            predecessorId = predecessor.getId();
        }

        return new LeadFlowDto(leadFlow.getId(), leadFlow.getLeadFlowOrder().getCompany().getId(),
                leadFlow.getLeadFlowOrder().getStatus(), leadFlow.getName(), leadFlow.getIcon(), leadFlow.getButtonText(),
                leadFlow.getTitle(), leadFlow.getConfirmationMessageHeader(), leadFlow.getConfirmationMessage1(),
                leadFlow.getConfirmationMessage2(), leadFlow.getConfirmationMessage3(),
                leadFlow.getLeadFlowOrder().getOrdinal(),
                leadFlow.getQuestions().stream()
                        .map(this::map).toList(), predecessorId);
    }

    public LeadFlowQuestionDto map(LeadFlowQuestion question) {
        return switch (question.getDataType()) {
            case BOOLEAN -> new LeadFlowBooleanQuestionDto(question.getId(), question.getQuestion(), question.getDataType(), question.getIsRequired(), question.getFalseText(), question.getTrueText());
            default -> new LeadFlowQuestionAnswerDto(question.getId(), question.getQuestion(), question.getDataType(), question.getIsRequired());
        };
    }

    public LeadFlowQuestion map(LeadFlowQuestionDto dto) {
        LeadFlowQuestion.LeadFlowQuestionBuilder builder = LeadFlowQuestion.builder().id(dto.getId()).question(dto.getQuestion()).dataType(dto.getDataType()).isRequired(dto.getIsRequired());

        if(dto.getDataType() == LeadFlowQuestionDataType.BOOLEAN) {
            builder.falseText(((LeadFlowBooleanQuestionDto)dto).getFalseText());
            builder.trueText(((LeadFlowBooleanQuestionDto)dto).getTrueText());
        }

        return builder.build();
    }

    public LeadFlowQuestion map(LeadFlowQuestionDto dto, LeadFlow leadFlow) {
        LeadFlowQuestion leadFlowQuestion = map(dto);
        leadFlowQuestion.setLeadFlow(leadFlow);

        return leadFlowQuestion;
    }

    public LeadFlow map(LeadFlowDto dto) {
        LeadFlow leadFlow = LeadFlow.builder().id(dto.id()).name(dto.name()).icon(dto.icon())
                .buttonText(dto.buttonText()).title(dto.title())
                .confirmationMessageHeader(dto.confirmationMessageHeader())
                .confirmationMessage1(dto.confirmationMessage1())
                .confirmationMessage2(dto.confirmationMessage2())
                .confirmationMessage3(dto.confirmationMessage3()).build();

        LeadFlowOrder leadFlowOrder = LeadFlowOrder.builder().ordinal(dto.ordinal()).status(dto.status()).leadFlow(leadFlow).build();
        leadFlow.setLeadFlowOrder(leadFlowOrder);

        List<LeadFlowQuestion> leadFlowQuestionList = dto.questions().stream().map(questionDto -> map(questionDto, leadFlow)).toList();
        leadFlow.setQuestions(leadFlowQuestionList);

        // DO NOT MAP predecessor_id from inbound dto

        return leadFlow;
    }

    public LeadFlowListDto map(Page<LeadFlow> pageOfLeadFlows) {
        PageableWrapper pageable = new PageableWrapper(pageOfLeadFlows.getPageable());
        return new LeadFlowListDto(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageOfLeadFlows.getTotalElements(),
                pageable.getSortField(),
                pageable.getSortDir(),
                pageOfLeadFlows.getContent().stream().map(leadFlow -> new LeadFlowListDto.LeadFlowListItem(leadFlow.getId(), leadFlow.getName(), leadFlow.getLeadFlowOrder().getStatus(), leadFlow.getLeadFlowOrder().getOrdinal())).toList()
        );
    }
}
