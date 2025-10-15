package com.rrsgroup.customer.web;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.customer.domain.LeadFlowStatus;
import com.rrsgroup.customer.domain.lead.LeadStatus;
import com.rrsgroup.customer.dto.LeadFlowDto;
import com.rrsgroup.customer.dto.LeadFlowQuestionDto;
import com.rrsgroup.customer.dto.lead.LeadAnswerDto;
import com.rrsgroup.customer.dto.lead.LeadDto;
import com.rrsgroup.customer.dto.lead.LeadListDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.QrCode;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.service.CustomerService;
import com.rrsgroup.customer.service.LeadFlowService;
import com.rrsgroup.customer.service.QrCodeService;
import com.rrsgroup.customer.service.lead.LeadService;
import com.rrsgroup.customer.service.lead.LeadDtoMapper;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Log4j2
public class LeadController {
    private final LeadFlowService leadFlowService;
    private final CustomerService customerService;
    private final LeadDtoMapper leadDtoMapper;
    private final LeadService leadService;
    private final QrCodeService qrCodeService;

    @Autowired
    public LeadController(
            LeadFlowService leadFlowService,
            CustomerService customerService,
            LeadDtoMapper leadDtoMapper,
            LeadService leadService,
            QrCodeService qrCodeService) {
        this.leadFlowService = leadFlowService;
        this.customerService = customerService;
        this.leadDtoMapper = leadDtoMapper;
        this.leadService = leadService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/api/public/customers/qrCode/{qrCode}/leads")
    public LeadDto createLead(@PathVariable("qrCode") UUID qrCode, @Valid @RequestBody LeadDto request) {
        Customer customer = getCustomerFromQrCode(qrCode);
        LeadFlowDto leadFlow = getActiveLeadFlow(request.leadFlowId(), customer.getCrmConfig().getCompanyId());

        validateAnswersMatchQuestions(request, leadFlow);
        validateRequiredQuestionsAreAnswered(request, leadFlow);

        // Save lead
        Lead lead = leadDtoMapper.map(request, customer);
        return leadDtoMapper.map(leadService.createLeadAnonymous(lead));
    }

    private void validateRequiredQuestionsAreAnswered(LeadDto request, LeadFlowDto leadFlow) {
        // Validate answers for all required questions exist in request
        Map<Long, LeadAnswerDto> leadAnswerDtoToQuestionIdMap = request.answers().stream().collect(Collectors.toMap(LeadAnswerDto::getLeadFlowQuestionId, Function.identity()));
        List<Long> missingRequiredQuestions = leadFlow.questions().stream().filter(LeadFlowQuestionDto::isRequired).map(LeadFlowQuestionDto::id).filter(requiredId -> !leadAnswerDtoToQuestionIdMap.containsKey(requiredId)).toList();

        if(!missingRequiredQuestions.isEmpty()) {
            throw new IllegalRequestException("The following leadFlowQuestionIds are required but not provided: " + missingRequiredQuestions);
        }
    }

    private void validateAnswersMatchQuestions(LeadDto request, LeadFlowDto leadFlow) {
        // Validate answers for questionIds related to lead flow
        Map<Long, LeadFlowQuestionDto> questionDtoMap =  leadFlow.questions().stream().collect(Collectors.toMap(LeadFlowQuestionDto::id, Function.identity()));
        List<Long> invalidIds = request.answers().stream()
                .map(LeadAnswerDto::getLeadFlowQuestionId)
                .filter(id -> !questionDtoMap.containsKey(id))
                .toList();

        if(!invalidIds.isEmpty()) {
            throw new IllegalRequestException("The following leadFlowQuestionIds in the answers do not have a matching question in the lead flow: " + invalidIds);
        }

        // Validate answers for questionIds are in the correct format
        List<String> invalidTypeMessages = request.answers().stream().filter(answer -> {
            LeadFlowQuestionDto question = questionDtoMap.get(answer.getLeadFlowQuestionId());
            return !answer.getDataType().equals(question.dataType());
        }).map(invalidTypeAnswer -> {
            LeadFlowQuestionDto question = questionDtoMap.get(invalidTypeAnswer.getLeadFlowQuestionId());
            return "Answer for leadFlowQuestionId=" + invalidTypeAnswer.getLeadFlowQuestionId() + " sent dataType=" + invalidTypeAnswer.getDataType() + ", but lead flow expected dataType=" + question.dataType();
        }).toList();

        if(!invalidTypeMessages.isEmpty()) {
            throw new IllegalRequestException("The following answers were of the wrong type: " + invalidTypeMessages);
        }
    }

    private LeadFlowDto getActiveLeadFlow(Long leadFlowId, Long companyId) {
        // Get lead flow for company; validate it exists and it is active
        Optional<LeadFlowDto> leadFlowOptional = leadFlowService.getLeadFlow(leadFlowId, companyId);

        if(leadFlowOptional.isEmpty()) {
            throw new RecordNotFoundException("Lead flow does not exist by leadFlowId=" + leadFlowId + ", companyId=" + companyId);
        }

        LeadFlowDto leadFlow = leadFlowOptional.get();

        if(leadFlow.status() != LeadFlowStatus.ACTIVE) {
            throw new IllegalUpdateException("Cannot create lead against an inactive lead flow");
        }
        return leadFlow;
    }

    private Customer getCustomerFromQrCode(UUID qrCode) {
        // Get customer by QR code; validate customer exists
        Optional<QrCode> qrCodeOptional = qrCodeService.getAssociatedQrCode(qrCode);

        if(qrCodeOptional.isEmpty()) {
            throw new RecordNotFoundException("Customer does not exist with qrCode=" + qrCode);
        }

        return qrCodeOptional.get().getCustomer();
    }

    @GetMapping("/api/admin/leads")
    public LeadListDto getListOfLeads(
            @AuthenticationPrincipal AdminUserDto user,
            @RequestParam(name = "status", required = false) List<LeadStatus> statuses,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "sortField", required = false, defaultValue = "id") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "DESC") SortDirection sortDir) {
        Long companyId = user.getCompanyId();
        Page<Lead> pageOfLeads = leadService.getCompanyListOfLeads(companyId, statuses, limit, page, sortField, sortDir);

        return leadDtoMapper.map(pageOfLeads);
    }

    @GetMapping("/api/admin/leads/{leadId}")
    public LeadDto getLead(@AuthenticationPrincipal AdminUserDto user, @PathVariable("leadId") Long leadId) {
        Optional<Lead> leadOptional = leadService.getLeadById(leadId, user);

        if(leadOptional.isEmpty()) {
            throw new RecordNotFoundException("Lead not found by leadId=" + leadId);
        }

        return leadDtoMapper.map(leadOptional.get());
    }
}
