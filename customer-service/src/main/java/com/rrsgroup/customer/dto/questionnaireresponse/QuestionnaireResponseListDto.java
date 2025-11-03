package com.rrsgroup.customer.dto.questionnaireresponse;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.PaginatedDto;
import com.rrsgroup.customer.domain.lead.LeadStatus;
import com.rrsgroup.customer.domain.questionnaireresponse.QuestionnaireResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionnaireResponseListDto extends PaginatedDto {
    private List<QuestionnaireResponseListDto.QuestionnaireResponseListItem> questionnaireResponses;

    public QuestionnaireResponseListDto(Integer page, Integer limit, Long total, String sortField, SortDirection sortDir, List<QuestionnaireResponseListDto.QuestionnaireResponseListItem> questionnaireResponses) {
        super(page, limit, total, sortField, sortDir);
        this.questionnaireResponses = questionnaireResponses;
    }

    @Data
    @AllArgsConstructor
    public static class QuestionnaireResponseListItem {
        private Long id;
        private String questionnaireName;
        private QuestionnaireResponseStatus status;
        private Long predecessorId;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;
    }
}
