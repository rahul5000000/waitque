package com.rrsgroup.customer.service;

import com.rrsgroup.common.util.PageableWrapper;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.dto.questionnaire.QuestionnaireDto;
import com.rrsgroup.customer.dto.questionnaireresponse.*;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.questionnaireresponse.QuestionnaireResponse;
import com.rrsgroup.customer.entity.questionnaireresponse.QuestionnaireResponseAnswer;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class QuestionnaireResponseDtoMapper {
    private final CustomerCrmIntegrationService customerCrmIntegrationService;
    private final QuestionnaireService questionnaireService;

    @Autowired
    public QuestionnaireResponseDtoMapper(
            CustomerCrmIntegrationService customerCrmIntegrationService,
            QuestionnaireService questionnaireService) {
        this.customerCrmIntegrationService = customerCrmIntegrationService;
        this.questionnaireService = questionnaireService;
    }

    public QuestionnaireResponse map(QuestionnaireResponseDto dto, Customer customer) {
        QuestionnaireResponse response = QuestionnaireResponse.builder().questionnaireId(dto.questionnaireId())
                .status(dto.status()).customer(customer).build();

        List<QuestionnaireResponseAnswer> answers = dto.answers().stream().map(answer -> map(answer, response)).toList();
        response.setAnswers(answers);

        return response;
    }

    public QuestionnaireResponseAnswer map(QuestionnaireResponseAnswerDto dto, QuestionnaireResponse response) {
        QuestionnaireResponseAnswer.QuestionnaireResponseAnswerBuilder builder = QuestionnaireResponseAnswer.builder().questionnaireResponse(response).questionnaireQuestionId(dto.getQuestionnaireQuestionId()).dataType(dto.getDataType());

        switch (dto.getDataType()) {
            case BOOLEAN -> builder.booleanAnswer(((QuestionnaireResponseBooleanAnswerDto)dto).getEnabled());
            case TEXT -> builder.textAnswer(((QuestionnaireResponseTextAnswerDto)dto).getText());
            case TEXTAREA -> builder.textAreaAnswer(((QuestionnaireResponseTextAreaAnswerDto)dto).getParagraph());
            case IMAGE -> builder.imageUrl(((QuestionnaireResponseImageAnswerDto)dto).getUrl());
            case NUMBER -> builder.numberAnswer(((QuestionnaireResponseNumberAnswerDto)dto).getNumber());
            case DECIMAL -> builder.decimalAnswer(((QuestionnaireResponseDecimalAnswerDto)dto).getDecimal());
            case PHONE -> builder.phoneAnswer(((QuestionnaireResponsePhoneAnswerDto)dto).getPhoneNumber());
            case EMAIL -> builder.emailAnswer(((QuestionnaireResponseEmailAnswerDto)dto).getEmail());
            default -> throw new IllegalStateException("Unknown data type: " + dto.getDataType());
        }

        return builder.build();
    }

    public QuestionnaireResponseDto map(QuestionnaireResponse response) {
        Optional<CrmCustomer> crmCustomerOptional = customerCrmIntegrationService.getCrmCustomer(response.getCustomer().getCrmCustomerId(), response.getCustomer().getCrmConfig());

        if(crmCustomerOptional.isEmpty()) {
            log.error("Did not find matching crmCustomer for customerId={}, crmCustomerId={}", response.getCustomer().getId(), response.getCustomer().getCrmCustomerId());
        }

        Optional<QuestionnaireDto> questionnaireOptional = questionnaireService.getQuestionnaire(response.getQuestionnaireId(), response.getCustomer().getCrmConfig().getCompanyId());

        if(questionnaireOptional.isEmpty()) {
            log.error("Did not find matching questionnaire for questionnaireId={}", response.getQuestionnaireId());
        }

        CrmCustomer crmCustomer = crmCustomerOptional.get();
        QuestionnaireDto questionnaire = questionnaireOptional.get();
        Long predecessorId = response.getPredecessor() != null ? response.getPredecessor().getId() : null;

        return new QuestionnaireResponseDto(response.getId(), response.getQuestionnaireId(), response.getStatus(),
                questionnaire,  crmCustomer, response.getAnswers().stream().map(this::map).toList(), predecessorId,
                response.getCreatedDate(), response.getUpdatedDate(), response.getCreatedBy(), response.getUpdatedBy());
    }

    public QuestionnaireResponseAnswerDto map(QuestionnaireResponseAnswer answer) {
        return switch (answer.getDataType()) {
            case BOOLEAN -> new QuestionnaireResponseBooleanAnswerDto(answer.getId(), answer.getQuestionnaireQuestionId(), answer.getBooleanAnswer());
            case TEXT -> new QuestionnaireResponseTextAnswerDto(answer.getId(), answer.getQuestionnaireQuestionId(), answer.getTextAnswer());
            case TEXTAREA -> new QuestionnaireResponseTextAreaAnswerDto(answer.getId(), answer.getQuestionnaireQuestionId(), answer.getTextAreaAnswer());
            case IMAGE -> new QuestionnaireResponseImageAnswerDto(answer.getId(), answer.getQuestionnaireQuestionId(), answer.getImageUrl());
            case NUMBER -> new QuestionnaireResponseNumberAnswerDto(answer.getId(), answer.getQuestionnaireQuestionId(), answer.getNumberAnswer());
            case DECIMAL -> new QuestionnaireResponseDecimalAnswerDto(answer.getId(), answer.getQuestionnaireQuestionId(), answer.getDecimalAnswer());
            case PHONE -> new QuestionnaireResponsePhoneAnswerDto(answer.getId(), answer.getQuestionnaireQuestionId(), answer.getPhoneAnswer());
            case EMAIL -> new QuestionnaireResponseEmailAnswerDto(answer.getId(), answer.getQuestionnaireQuestionId(), answer.getEmailAnswer());
            default -> throw new IllegalStateException("Unknown data type: " + answer.getDataType());
        };
    }

    public QuestionnaireResponseListDto map(Page<QuestionnaireResponse> pageOfQuestionnaireResponses) {
        PageableWrapper pageable = new PageableWrapper(pageOfQuestionnaireResponses.getPageable());
        return new QuestionnaireResponseListDto(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageOfQuestionnaireResponses.getTotalElements(),
                pageable.getSortField(),
                pageable.getSortDir(),
                pageOfQuestionnaireResponses.getContent().stream().map(response -> {
                    Optional<QuestionnaireDto> questionnaireOptional = questionnaireService.getQuestionnaire(response.getQuestionnaireId(), response.getCustomer().getCrmConfig().getCompanyId());

                    if(questionnaireOptional.isEmpty()) {
                        log.error("Did not find matching questionnaire for questionnaireId={}", response.getQuestionnaireId());
                    }

                    QuestionnaireDto questionnaire = questionnaireOptional.get();
                    Long predecessorId = response.getPredecessor() != null ? response.getPredecessor().getId() : null;

                    return new QuestionnaireResponseListDto.QuestionnaireResponseListItem(response.getId(), questionnaire.name(),
                            response.getStatus(), predecessorId, response.getCreatedDate(), response.getUpdatedDate());
                }).toList()
        );
    }
}
