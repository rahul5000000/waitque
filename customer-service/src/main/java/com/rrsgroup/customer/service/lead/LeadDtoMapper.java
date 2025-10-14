package com.rrsgroup.customer.service.lead;

import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.common.service.CommonDtoMapper;
import com.rrsgroup.customer.dto.lead.*;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.entity.lead.LeadAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadDtoMapper {
    private final CommonDtoMapper commonDtoMapper;

    @Autowired
    public LeadDtoMapper(CommonDtoMapper commonDtoMapper) {
        this.commonDtoMapper = commonDtoMapper;
    }

    public Lead map(LeadDto dto, Customer customer) {
        Lead.LeadBuilder builder = Lead.builder().leadFlowId(dto.leadFlowId()).status(dto.status()).overrideFirstName(dto.overrideFirstName())
                .overrideLastName(dto.overrideLastName()).overrideEmail(dto.overrideEmail()).customer(customer);

        if(dto.overrideAddress() != null) {
            builder.overrideAddress(commonDtoMapper.map(dto.overrideAddress()));
        }

        if(dto.overridePhoneNumber() != null) {
            builder.overridePhoneNumber(commonDtoMapper.map(dto.overridePhoneNumber()));
        }

        Lead lead = builder.build();

        List<LeadAnswer> answers = dto.answers().stream().map(answer -> map(answer, lead)).toList();
        lead.setAnswers(answers);

        return lead;
    }

    public LeadAnswer map(LeadAnswerDto dto, Lead lead) {
        LeadAnswer.LeadAnswerBuilder builder = LeadAnswer.builder().lead(lead).leadFlowQuestionId(dto.getLeadFlowQuestionId()).dataType(dto.getDataType());

        switch (dto.getDataType()) {
            case BOOLEAN -> builder.booleanAnswer(((LeadBooleanAnswerDto)dto).getEnabled());
            case TEXT -> builder.textAnswer(((LeadTextAnswerDto)dto).getText());
            case TEXTAREA -> builder.textAreaAnswer(((LeadTextAreaAnswerDto)dto).getParagraph());
            case IMAGE -> builder.imageUrl(((LeadImageAnswerDto)dto).getUrl());
            case NUMBER -> builder.numberAnswer(((LeadNumberAnswerDto)dto).getNumber());
            case DECIMAL -> builder.decimalAnswer(((LeadDecimalAnswerDto)dto).getDecimal());
            default -> throw new IllegalStateException("Unknown data type: " + dto.getDataType());
        }

        return builder.build();
    }

    public LeadAnswerDto map(LeadAnswer answer) {
        return switch (answer.getDataType()) {
            case BOOLEAN -> new LeadBooleanAnswerDto(answer.getId(), answer.getLeadFlowQuestionId(), answer.getBooleanAnswer());
            case TEXT -> new LeadTextAnswerDto(answer.getId(), answer.getLeadFlowQuestionId(), answer.getTextAnswer());
            case TEXTAREA -> new LeadTextAreaAnswerDto(answer.getId(), answer.getLeadFlowQuestionId(), answer.getTextAreaAnswer());
            case IMAGE -> new LeadImageAnswerDto(answer.getId(), answer.getLeadFlowQuestionId(), answer.getImageUrl());
            case NUMBER -> new LeadNumberAnswerDto(answer.getId(), answer.getLeadFlowQuestionId(), answer.getNumberAnswer());
            case DECIMAL -> new LeadDecimalAnswerDto(answer.getId(), answer.getLeadFlowQuestionId(), answer.getDecimalAnswer());
            default -> throw new IllegalStateException("Unknown data type: " + answer.getDataType());
        };
    }

    public LeadDto map(Lead lead) {
        AddressDto addressDto = lead.getOverrideAddress() == null ? null : commonDtoMapper.map(lead.getOverrideAddress());
        PhoneNumberDto phoneNumberDto = lead.getOverridePhoneNumber() == null ? null : commonDtoMapper.map(lead.getOverridePhoneNumber());
        return new LeadDto(lead.getId(), lead.getLeadFlowId(),
                lead.getStatus(), lead.getOverrideFirstName(), lead.getOverrideLastName(), addressDto, phoneNumberDto,
                lead.getOverrideEmail(), lead.getAnswers().stream().map(this::map).toList(), null,
                lead.getCreatedDate(), lead.getUpdatedDate(), lead.getCreatedBy(), lead.getUpdatedBy());
    }
}
