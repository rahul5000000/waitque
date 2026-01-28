package com.rrsgroup.customer.service.lead;

import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.common.service.CommonDtoMapper;
import com.rrsgroup.common.util.PageableWrapper;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.dto.LeadFlowDto;
import com.rrsgroup.customer.dto.lead.*;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.entity.lead.LeadAnswer;
import com.rrsgroup.customer.service.CustomerCrmIntegrationService;
import com.rrsgroup.customer.service.LeadFlowService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class LeadDtoMapper {
    @Value("${CDN_BASE_URL}")
    private String cdnBaseUrl;

    private final CommonDtoMapper commonDtoMapper;
    private final CustomerCrmIntegrationService customerCrmIntegrationService;
    private final LeadFlowService leadFlowService;

    @Autowired
    public LeadDtoMapper(
            CommonDtoMapper commonDtoMapper,
            CustomerCrmIntegrationService customerCrmIntegrationService,
            LeadFlowService leadFlowService) {
        this.commonDtoMapper = commonDtoMapper;
        this.customerCrmIntegrationService = customerCrmIntegrationService;
        this.leadFlowService = leadFlowService;
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
        Optional<CrmCustomer> crmCustomerOptional = customerCrmIntegrationService.getCrmCustomer(lead.getCustomer().getCrmCustomerId(), lead.getCustomer().getCrmConfig());

        if(crmCustomerOptional.isEmpty()) {
            log.error("Did not find matching crmCustomer for customerId={}, crmCustomerId={}", lead.getCustomer().getId(), lead.getCustomer().getCrmCustomerId());
        }

        Optional<LeadFlowDto> leadFlowOptional = leadFlowService.getLeadFlow(lead.getLeadFlowId(), lead.getCustomer().getCrmConfig().getCompanyId());

        if(leadFlowOptional.isEmpty()) {
            log.error("Did not find matching leadFlow for leadFlowId={}", lead.getLeadFlowId());
        }

        CrmCustomer crmCustomer = crmCustomerOptional.get();
        LeadFlowDto leadFlow = leadFlowOptional.get();

        AddressDto addressDto = lead.getOverrideAddress() == null ? null : commonDtoMapper.map(lead.getOverrideAddress());
        PhoneNumberDto phoneNumberDto = lead.getOverridePhoneNumber() == null ? null : commonDtoMapper.map(lead.getOverridePhoneNumber());
        return new LeadDto(lead.getId(), lead.getLeadFlowId(),
                lead.getStatus(), lead.getOverrideFirstName(), lead.getOverrideLastName(), addressDto, phoneNumberDto,
                lead.getOverrideEmail(), lead.getAnswers().stream().map(this::map).toList(), crmCustomer, leadFlow,
                null, cdnBaseUrl, lead.getCreatedDate(), lead.getUpdatedDate(), lead.getCreatedBy(), lead.getUpdatedBy());
    }

    public LeadListDto map(Page<Lead> pageOfLeads) {
        PageableWrapper pageable = new PageableWrapper(pageOfLeads.getPageable());
        return new LeadListDto(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageOfLeads.getTotalElements(),
                pageable.getSortField(),
                pageable.getSortDir(),
                pageOfLeads.getContent().stream().map(lead -> {
                    Optional<CrmCustomer> crmCustomerOptional = customerCrmIntegrationService.getCrmCustomer(lead.getCustomer().getCrmCustomerId(), lead.getCustomer().getCrmConfig());

                    if(crmCustomerOptional.isEmpty()) {
                        log.error("Did not find matching crmCustomer for customerId={}, crmCustomerId={}", lead.getCustomer().getId(), lead.getCustomer().getCrmCustomerId());
                        return null;
                    }

                    Optional<LeadFlowDto> leadFlowOptional = leadFlowService.getLeadFlow(lead.getLeadFlowId(), lead.getCustomer().getCrmConfig().getCompanyId());

                    if(leadFlowOptional.isEmpty()) {
                        log.error("Did not find matching leadFlow for leadFlowId={}", lead.getLeadFlowId());
                        return null;
                    }

                    CrmCustomer crmCustomer = crmCustomerOptional.get();
                    LeadFlowDto leadFlow = leadFlowOptional.get();

                    String companyName = crmCustomer.getCompanyName();
                    String firstName = StringUtils.isBlank(lead.getOverrideFirstName()) ? crmCustomer.getFirstName() : lead.getOverrideFirstName();
                    String lastName = StringUtils.isBlank(lead.getOverrideLastName()) ? crmCustomer.getLastName() : lead.getOverrideLastName();
                    String leadFlowName = leadFlow.name();
                    String phoneNumber = lead.getOverridePhoneNumber() == null ? crmCustomer.getPhoneNumber().toString() : lead.getOverridePhoneNumber().toString();
                    String email = StringUtils.isBlank(lead.getOverrideEmail()) ? crmCustomer.getEmail() : lead.getOverrideEmail();

                    return new LeadListDto.LeadListItem(lead.getId(), companyName, firstName, lastName, leadFlowName, phoneNumber, email, lead.getStatus(), lead.getCreatedDate(), lead.getUpdatedDate());
                }).toList()
        );
    }
}
