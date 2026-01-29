package com.rrsgroup.customer.web;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.common.service.S3Service;
import com.rrsgroup.customer.domain.FileStage;
import com.rrsgroup.customer.domain.LeadFlowStatus;
import com.rrsgroup.customer.domain.UploadFileType;
import com.rrsgroup.customer.domain.lead.LeadStatus;
import com.rrsgroup.customer.dto.LeadFlowDto;
import com.rrsgroup.customer.dto.LeadFlowQuestionDto;
import com.rrsgroup.customer.dto.UploadUrlDto;
import com.rrsgroup.customer.dto.lead.LeadAnswerDto;
import com.rrsgroup.customer.dto.lead.LeadDto;
import com.rrsgroup.customer.dto.lead.LeadListDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.service.CustomerService;
import com.rrsgroup.customer.service.LeadFlowService;
import com.rrsgroup.customer.service.NotificationService;
import com.rrsgroup.customer.service.UploadUrlDtoMapper;
import com.rrsgroup.customer.service.lead.LeadDtoMapper;
import com.rrsgroup.customer.service.lead.LeadService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.time.LocalDateTime;
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
    private final S3Service s3Service;
    private final UploadUrlDtoMapper uploadUrlDtoMapper;
    private final NotificationService notificationService;

    @Autowired
    public LeadController(
            LeadFlowService leadFlowService,
            CustomerService customerService,
            LeadDtoMapper leadDtoMapper,
            LeadService leadService,
            S3Service s3Service,
            UploadUrlDtoMapper uploadUrlDtoMapper,
            NotificationService notificationService) {
        this.leadFlowService = leadFlowService;
        this.customerService = customerService;
        this.leadDtoMapper = leadDtoMapper;
        this.leadService = leadService;
        this.s3Service = s3Service;
        this.uploadUrlDtoMapper = uploadUrlDtoMapper;
        this.notificationService = notificationService;
    }

    @PostMapping("/api/public/customers/qrCode/{qrCode}/leads")
    public LeadDto createLead(@PathVariable("qrCode") UUID qrCode, @Valid @RequestBody LeadDto request) {
        Customer customer = customerService.getCustomerByQrCodeSafe(qrCode);
        LeadFlowDto leadFlow = getActiveLeadFlow(request.leadFlowId(), customer.getCrmConfig().getCompanyId());

        validateAnswersMatchQuestions(request, leadFlow);
        validateRequiredQuestionsAreAnswered(request, leadFlow);

        // Save lead
        Lead lead = leadDtoMapper.map(request, customer);
        Lead savedLead = leadService.createLeadAnonymous(lead);

        notificationService.sendNotification(savedLead, leadFlow, customer);

        return leadDtoMapper.map(savedLead);
    }

    @GetMapping("/api/public/customers/qrCode/{qrCode}/leads/photoUploadUrl")
    public UploadUrlDto generateLeadUploadUrl(
            @PathVariable("qrCode") UUID qrCode,
            @RequestParam(name = "fileName") String fileName,
            @RequestParam(name = "contentType") String contentType) {
        Customer customer = customerService.getCustomerByQrCodeSafe(qrCode);

        log.info("Uploading photo with fileName={} for customerId={}", fileName, customer.getId());

        int validity = 300;
        LocalDateTime validUntil = LocalDateTime.now().plusSeconds(validity);
        String bucketKey = customerService.getBucketKeyForFileAndStage(qrCode, UploadFileType.LEAD, fileName, FileStage.RAW);

        URL url = s3Service.generateUploadUrl(S3Service.WAITQUE_UPLOAD_BUCKET, bucketKey, contentType, validity);

        return uploadUrlDtoMapper.map(url, bucketKey, validUntil);
    }

    @DeleteMapping("/api/public/customers/qrCode/{qrCode}/leads/photoUpload")
    public void deleteLeadPhotoUpload(@PathVariable("qrCode") UUID qrCode, @RequestParam("photoPath") String path) {
        Customer customer = customerService.getCustomerByQrCodeSafe(qrCode);

        log.info("Deleting photo with path={} for customerId={}", path, customer.getId());

        if(!path.contains("/" + qrCode + "/")) {
            throw new IllegalRequestException("Cannot delete photo for another customer");
        }

        s3Service.delete(S3Service.WAITQUE_UPLOAD_BUCKET, path);
    }

    private void validateRequiredQuestionsAreAnswered(LeadDto request, LeadFlowDto leadFlow) {
        // Validate answers for all required questions exist in request
        Map<Long, LeadAnswerDto> leadAnswerDtoToQuestionIdMap = request.answers().stream().collect(Collectors.toMap(LeadAnswerDto::getLeadFlowQuestionId, Function.identity()));
        List<Long> missingRequiredQuestions = leadFlow.questions().stream().filter(LeadFlowQuestionDto::getIsRequired).map(LeadFlowQuestionDto::getId).filter(requiredId -> !leadAnswerDtoToQuestionIdMap.containsKey(requiredId)).toList();

        if(!missingRequiredQuestions.isEmpty()) {
            throw new IllegalRequestException("The following leadFlowQuestionIds are required but not provided: " + missingRequiredQuestions);
        }
    }

    private void validateAnswersMatchQuestions(LeadDto request, LeadFlowDto leadFlow) {
        // Validate answers for questionIds related to lead flow
        Map<Long, LeadFlowQuestionDto> questionDtoMap =  leadFlow.questions().stream().collect(Collectors.toMap(LeadFlowQuestionDto::getId, Function.identity()));
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
            return !answer.getDataType().equals(question.getDataType());
        }).map(invalidTypeAnswer -> {
            LeadFlowQuestionDto question = questionDtoMap.get(invalidTypeAnswer.getLeadFlowQuestionId());
            return "Answer for leadFlowQuestionId=" + invalidTypeAnswer.getLeadFlowQuestionId() + " sent dataType=" + invalidTypeAnswer.getDataType() + ", but lead flow expected dataType=" + question.getDataType();
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

    @PatchMapping("/api/admin/leads/{leadId}/status")
    public LeadDto updateLeadStatus(
            @AuthenticationPrincipal AdminUserDto user,
            @PathVariable("leadId") Long leadId,
            @RequestParam("status") LeadStatus status) {
        Optional<Lead> leadOptional = leadService.getLeadById(leadId, user);
        if (leadOptional.isEmpty()) {
            throw new RecordNotFoundException("Lead not found by leadId=" + leadId);
        }
        Lead lead = leadOptional.get();

        return leadDtoMapper.map(leadService.updateLeadStatus(lead, status, user));
    }
}
