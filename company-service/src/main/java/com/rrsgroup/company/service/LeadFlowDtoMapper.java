package com.rrsgroup.company.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.company.dto.LeadFlowDto;
import com.rrsgroup.company.dto.LeadFlowListDto;
import com.rrsgroup.company.dto.LeadFlowQuestionDto;
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
        return new LeadFlowDto(leadFlow.getId(), leadFlow.getLeadFlowOrder().getCompany().getId(),
                leadFlow.getLeadFlowOrder().getStatus(), leadFlow.getName(), leadFlow.getIcon(), leadFlow.getButtonText(),
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

    public LeadFlowQuestion map(LeadFlowQuestionDto dto, LeadFlow leadFlow) {
        LeadFlowQuestion leadFlowQuestion = map(dto);
        leadFlowQuestion.setLeadFlow(leadFlow);

        return leadFlowQuestion;
    }

    public LeadFlow map(LeadFlowDto dto) {
        LeadFlow leadFlow = LeadFlow.builder().id(dto.id()).name(dto.name()).icon(dto.iconUrl())
                .buttonText(dto.buttonText()).title(dto.title())
                .confirmationMessageHeader(dto.confirmationMessageHeader())
                .confirmationMessage1(dto.confirmationMessage1())
                .confirmationMessage2(dto.confirmationMessage2())
                .confirmationMessage3(dto.confirmationMessage3()).build();

        LeadFlowOrder leadFlowOrder = LeadFlowOrder.builder().ordinal(dto.ordinal()).status(dto.status()).leadFlow(leadFlow).build();
        leadFlow.setLeadFlowOrder(leadFlowOrder);

        List<LeadFlowQuestion> leadFlowQuestionList = dto.questions().stream().map(questionDto -> map(questionDto, leadFlow)).toList();
        leadFlow.setQuestions(leadFlowQuestionList);

        return leadFlow;
    }

    public LeadFlowListDto map(Page<LeadFlow> pageOfLeadFlows) {
        String sortField = pageOfLeadFlows.getPageable().getSort().stream().findFirst()
                .map(Sort.Order::getProperty).orElse("");
        SortDirection sortDir = pageOfLeadFlows.getPageable().getSort().stream().findFirst()
                .map(sort -> sort.getDirection() == Sort.Direction.ASC ? SortDirection.ASC : SortDirection.DESC).orElse(SortDirection.ASC);
        sortField = getLastPart(sortField);
        return new LeadFlowListDto(
                pageOfLeadFlows.getPageable().getPageNumber(),
                pageOfLeadFlows.getPageable().getPageSize(),
                pageOfLeadFlows.getTotalElements(),
                sortField,
                sortDir,
                pageOfLeadFlows.getContent().stream().map(leadFlow -> new LeadFlowListDto.LeadFlowListItem(leadFlow.getId(), leadFlow.getName(), leadFlow.getLeadFlowOrder().getStatus(), leadFlow.getLeadFlowOrder().getOrdinal())).toList()
        );
    }

    private String getLastPart(String input) {
        if (input == null || input.isEmpty()) {
            return input; // return as-is for null/empty
        }
        int lastDot = input.lastIndexOf('.');
        if (lastDot == -1) {
            return input; // no dot, return whole string
        }
        return input.substring(lastDot + 1);
    }
}
