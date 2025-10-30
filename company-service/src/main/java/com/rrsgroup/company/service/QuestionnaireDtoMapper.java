package com.rrsgroup.company.service;

import com.rrsgroup.company.domain.questionnaire.QuestionnaireQuestionDataType;
import com.rrsgroup.company.dto.questionnaire.*;
import com.rrsgroup.company.entity.questionnaire.Questionnaire;
import com.rrsgroup.company.entity.questionnaire.QuestionnairePage;
import com.rrsgroup.company.entity.questionnaire.QuestionnaireQuestion;
import org.springframework.stereotype.Service;

@Service
public class QuestionnaireDtoMapper {
    public QuestionnaireDto map(Questionnaire questionnaire) {
        Long predecessorId = null;

        if(questionnaire.getPredecessor() != null) {
            predecessorId = questionnaire.getPredecessor().getId();
        }

        return new QuestionnaireDto(questionnaire.getId(), questionnaire.getCompany().getId(), questionnaire.getName(), questionnaire.getDescription(), questionnaire.getPages().stream().map(this::map).toList(), predecessorId);
    }

    public QuestionnairePageDto map(QuestionnairePage page) {
        return new QuestionnairePageDto(page.getId(), page.getPageTitle(), page.getPageNumber(), page.getQuestions().stream().map(this::map).toList());
    }

    public QuestionnaireQuestionDto map (QuestionnaireQuestion question) {
        if(question.getDataType() == QuestionnaireQuestionDataType.BOOLEAN) {
            return new QuestionnaireQuestionBooleanDto(question.getId(), question.getQuestion(), question.getDataType(), question.getIsRequired(), question.getQuestionGroup(), question.getFalseText(), question.getTrueText());
        } else {
            return new QuestionnaireQuestionAnswerDto(question.getId(), question.getQuestion(), question.getDataType(), question.getIsRequired(), question.getQuestionGroup());
        }
    }
}
