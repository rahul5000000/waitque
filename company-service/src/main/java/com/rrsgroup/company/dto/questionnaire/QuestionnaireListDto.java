package com.rrsgroup.company.dto.questionnaire;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.PaginatedDto;
import com.rrsgroup.company.domain.questionnaire.QuestionnaireStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class QuestionnaireListDto extends PaginatedDto {
    private List<QuestionnaireListDto.QuestionnairListItem> questionnaires;

    public QuestionnaireListDto(Integer page, Integer limit, Long total, String sortField, SortDirection sortDir, List<QuestionnaireListDto.QuestionnairListItem> questionnaires) {
        super(page, limit, total, sortField, sortDir);
        this.questionnaires = questionnaires;
    }

    @Data
    @AllArgsConstructor
    public static class QuestionnairListItem {
        private Long id;
        private Long companyId;
        private String name;
        private QuestionnaireStatus status;
    }
}
