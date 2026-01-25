package com.rrsgroup.customer.web;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.FieldUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.common.service.S3Service;
import com.rrsgroup.customer.domain.FileStage;
import com.rrsgroup.customer.domain.UploadFileType;
import com.rrsgroup.customer.domain.questionnaire.QuestionnaireStatus;
import com.rrsgroup.customer.domain.questionnaireresponse.QuestionnaireResponseStatus;
import com.rrsgroup.customer.dto.UploadUrlDto;
import com.rrsgroup.customer.dto.questionnaire.QuestionnaireDto;
import com.rrsgroup.customer.dto.questionnaire.QuestionnaireQuestionDto;
import com.rrsgroup.customer.dto.questionnaireresponse.QuestionnaireResponseAnswerDto;
import com.rrsgroup.customer.dto.questionnaireresponse.QuestionnaireResponseDto;
import com.rrsgroup.customer.dto.questionnaireresponse.QuestionnaireResponseListDto;
import com.rrsgroup.customer.dto.questionnaireresponse.QuestionnaireResponseStatusUpdateDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.questionnaireresponse.QuestionnaireResponse;
import com.rrsgroup.customer.service.*;
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
public class QuestionnaireResponseController {
    private final CustomerService customerService;
    private final QuestionnaireService questionnaireService;
    private final QuestionnaireResponseDtoMapper dtoMapper;
    private final QuestionnaireResponseService questionnaireResponseService;
    private final S3Service s3Service;
    private final UploadUrlDtoMapper uploadUrlDtoMapper;
    private final EventService eventService;

    @Autowired
    public QuestionnaireResponseController(
            CustomerService customerService,
            QuestionnaireService questionnaireService,
            QuestionnaireResponseDtoMapper dtoMapper,
            QuestionnaireResponseService questionnaireResponseService,
            S3Service s3Service,
            UploadUrlDtoMapper uploadUrlDtoMapper,
            EventService eventService) {
        this.customerService = customerService;
        this.questionnaireService = questionnaireService;
        this.dtoMapper = dtoMapper;
        this.questionnaireResponseService = questionnaireResponseService;
        this.s3Service = s3Service;
        this.uploadUrlDtoMapper = uploadUrlDtoMapper;
        this.eventService = eventService;
    }

    @PostMapping("/api/field/customers/{customerId}/questionnaires/{questionnaireId}/responses")
    public QuestionnaireResponseDto createQuestionnaireResponse(
            @AuthenticationPrincipal FieldUserDto fieldUserDto,
            @PathVariable("customerId") Long customerId,
            @PathVariable("questionnaireId") Long questionnaireId,
            @Valid @RequestBody QuestionnaireResponseDto request
    ) {
        if(request.questionnaireId() != null && !request.questionnaireId().equals(questionnaireId)) {
            throw new IllegalUpdateException("The questionnaireId in the request body must match the questionnaireId in the url");
        }

        // Does customer exist in user org?
        Optional<Customer> customerOptional = customerService.getCustomerById(customerId, fieldUserDto);
        if(customerOptional.isEmpty()) throw new RecordNotFoundException("Customer not found with customerId=" + customerId);
        Customer customer = customerOptional.get();

        QuestionnaireDto questionnaire = getActiveQuestionnaire(questionnaireId, customer.getCrmConfig().getCompanyId());

        validateAnswersMatchQuestions(request, questionnaire);
        validateRequiredQuestionsAreAnswered(request, questionnaire);

        QuestionnaireResponse questionnaireResponse = dtoMapper.map(request, customer);
        questionnaireResponse.setQuestionnaireId(questionnaireId);
        QuestionnaireResponseDto response = dtoMapper.map(questionnaireResponseService.createQuestionnaireResponse(questionnaireResponse, fieldUserDto));

        eventService.questionnaireResponseCreated(response, customer, fieldUserDto);

        return response;
    }

    private QuestionnaireDto getActiveQuestionnaire(Long questionnaireId, Long companyId) {
        Optional<QuestionnaireDto> questionnaireOptional = questionnaireService.getQuestionnaire(questionnaireId, companyId);

        if(questionnaireOptional.isEmpty()) throw new RecordNotFoundException("Questionnaire not found for questionnaireId=" + questionnaireId + ", companyId=" + companyId);

        QuestionnaireDto questionnaire = questionnaireOptional.get();

        if(questionnaire.status() == QuestionnaireStatus.INACTIVE) throw new IllegalUpdateException("Cannot create a response to an inactive questionnaire");

        return questionnaire;
    }

    private void validateAnswersMatchQuestions(QuestionnaireResponseDto request, QuestionnaireDto questionnaire) {
        // Validate answers for questionIds related to lead flow
        Map<Long, QuestionnaireQuestionDto> questionDtoMap =  questionnaire.pages().stream().flatMap(page -> page.questions().stream()).toList().stream().collect(Collectors.toMap(QuestionnaireQuestionDto::getId, Function.identity()));
        List<Long> invalidIds = request.answers().stream()
                .map(QuestionnaireResponseAnswerDto::getQuestionnaireQuestionId)
                .filter(id -> !questionDtoMap.containsKey(id))
                .toList();

        if(!invalidIds.isEmpty()) {
            throw new IllegalRequestException("The following questionnaireQuestionIds in the answers do not have a matching question in the questionnaire: " + invalidIds);
        }

        // Validate answers for questionIds are in the correct format
        List<String> invalidTypeMessages = request.answers().stream().filter(answer -> {
            QuestionnaireQuestionDto question = questionDtoMap.get(answer.getQuestionnaireQuestionId());
            return !answer.getDataType().equals(question.getDataType());
        }).map(invalidTypeAnswer -> {
            QuestionnaireQuestionDto question = questionDtoMap.get(invalidTypeAnswer.getQuestionnaireQuestionId());
            return "Answer for questionnaireQuestionIds=" + invalidTypeAnswer.getQuestionnaireQuestionId() + " sent dataType=" + invalidTypeAnswer.getDataType() + ", but questionnaire expected dataType=" + question.getDataType();
        }).toList();

        if(!invalidTypeMessages.isEmpty()) {
            throw new IllegalRequestException("The following answers were of the wrong type: " + invalidTypeMessages);
        }
    }

    private void validateRequiredQuestionsAreAnswered(QuestionnaireResponseDto request, QuestionnaireDto questionnaire) {
        // Validate answers for all required questions exist in request
        Map<Long, QuestionnaireResponseAnswerDto> answerDtoToQuestionIdMap = request.answers().stream().collect(Collectors.toMap(QuestionnaireResponseAnswerDto::getQuestionnaireQuestionId, Function.identity()));
        List<Long> missingRequiredQuestions = questionnaire.pages().stream().flatMap(page -> page.questions().stream()).toList().stream().filter(QuestionnaireQuestionDto::getIsRequired).map(QuestionnaireQuestionDto::getId).filter(requiredId -> !answerDtoToQuestionIdMap.containsKey(requiredId)).toList();

        if(!missingRequiredQuestions.isEmpty()) {
            throw new IllegalRequestException("The following questionnaireQuestionIds are required but not provided: " + missingRequiredQuestions);
        }
    }

    @GetMapping("/api/field/customers/{customerId}/questionnaires/*/responses")
    public QuestionnaireResponseListDto searchQuestionnaireResponses(
            @AuthenticationPrincipal FieldUserDto fieldUserDto,
            @PathVariable("customerId") Long customerId,
            @RequestParam(name = "status", required = false) List<QuestionnaireResponseStatus> statuses,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "sortField", required = false, defaultValue = "id") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "DESC") SortDirection sortDir
    ) {
        Long companyId = fieldUserDto.getCompanyId();
        Page<QuestionnaireResponse> pageOfQuestionnaireResponses = questionnaireResponseService.getCustomerListOfQuestionnaireResponses(customerId, companyId, statuses, limit, page, sortField, sortDir);

        return dtoMapper.map(pageOfQuestionnaireResponses);
    }

    @GetMapping("/api/public/customers/qrCode/{qrCode}/questionnaires/*/responses")
    public QuestionnaireResponseListDto publicSearchQuestionnaireResponses(
            @PathVariable(name = "qrCode") UUID qrCode,
            @RequestParam(name = "status", required = false) List<QuestionnaireResponseStatus> statuses,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "sortField", required = false, defaultValue = "id") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "DESC") SortDirection sortDir
    ) {
        Customer customer = customerService.getCustomerByQrCodeSafe(qrCode);
        Page<QuestionnaireResponse> pageOfQuestionnaireResponses = questionnaireResponseService.getCustomerListOfQuestionnaireResponses(customer.getId(), customer.getCrmConfig().getCompanyId(), statuses, limit, page, sortField, sortDir);

        return dtoMapper.map(pageOfQuestionnaireResponses);
    }

    @GetMapping("/api/field/customers/{customerId}/questionnaires/*/responses/{responseId}")
    public QuestionnaireResponseDto getQuestionnaireResponse(@AuthenticationPrincipal FieldUserDto fieldUserDto,
                                                             @PathVariable("customerId") Long customerId,
                                                             @PathVariable("responseId") Long responseId) {
        return dtoMapper.map(getQuestionnaireResponseSafe(fieldUserDto, customerId, responseId));
    }

    @GetMapping("/api/public/customers/qrCode/{qrCode}/questionnaires/*/responses/{responseId}")
    public QuestionnaireResponseDto publicGetQuestionnaireResponse(@PathVariable(name = "qrCode") UUID qrCode,
                                                             @PathVariable("responseId") Long responseId) {
        Customer customer = customerService.getCustomerByQrCodeSafe(qrCode);
        QuestionnaireResponseDto response = dtoMapper.map(getQuestionnaireResponseSafe(customer.getCrmConfig().getCompanyId(), customer.getId(), responseId));

        eventService.questionnaireResponseViewed(response, qrCode, customer);

        return response;
    }

    private QuestionnaireResponse getQuestionnaireResponseSafe(FieldUserDto fieldUserDto, Long customerId, Long responseId) {
        return getQuestionnaireResponseSafe(fieldUserDto.getCompanyId(), customerId, responseId);
    }

    private QuestionnaireResponse getQuestionnaireResponseSafe(Long companyId, Long customerId, Long responseId) {
        Optional<QuestionnaireResponse> questionnaireResponseOptional = questionnaireResponseService.getQuestionnaireResponseForCustomer(responseId, customerId, companyId);

        if(questionnaireResponseOptional.isEmpty()) {
            throw new RecordNotFoundException("Questionnaire response not found by responseId=" + responseId + ", customerId="+customerId + ", companyId=" + companyId);
        }

        return questionnaireResponseOptional.get();
    }

    @PutMapping("/api/field/customers/{customerId}/questionnaires/*/responses/{responseId}")
    public QuestionnaireResponseDto updateQuestionnaireResponse(@AuthenticationPrincipal FieldUserDto fieldUserDto,
                                                                @PathVariable("customerId") Long customerId,
                                                                @PathVariable("responseId") Long responseId,
                                                                @Valid @RequestBody QuestionnaireResponseDto request) {
        if(request.id() != null && !request.id().equals(responseId)) {
            throw new IllegalUpdateException("The ID in the request body does not match the id in the URL");
        }

        QuestionnaireResponse existingQuestionnaireResponse = getQuestionnaireResponseSafe(fieldUserDto, customerId, responseId);

        if(!existingQuestionnaireResponse.getQuestionnaireId().equals(request.questionnaireId())) {
            throw new IllegalUpdateException("The questionnaireId cannot be updated");
        }

        Customer customer = existingQuestionnaireResponse.getCustomer();
        Long questionnaireId = existingQuestionnaireResponse.getQuestionnaireId();
        QuestionnaireDto questionnaire = getActiveQuestionnaire(questionnaireId, customer.getCrmConfig().getCompanyId());

        validateAnswersMatchQuestions(request, questionnaire);
        validateRequiredQuestionsAreAnswered(request, questionnaire);

        QuestionnaireResponse questionnaireResponse = dtoMapper.map(request, customer);
        questionnaireResponse.setQuestionnaireId(questionnaireId);

        return dtoMapper.map(questionnaireResponseService.updateQuestionnaireResponse(questionnaireResponse, existingQuestionnaireResponse, fieldUserDto));
    }

    @DeleteMapping("/api/field/customers/{customerId}/questionnaires/*/responses/{responseId}")
    public QuestionnaireResponseDto deleteQuestionnaireResponse(@AuthenticationPrincipal FieldUserDto fieldUserDto,
                                                                @PathVariable("customerId") Long customerId,
                                                                @PathVariable("responseId") Long responseId) {
        QuestionnaireResponse existingQuestionnaireResponse = getQuestionnaireResponseSafe(fieldUserDto, customerId, responseId);
        return dtoMapper.map(questionnaireResponseService.markQuestionnaireResponseStatus(existingQuestionnaireResponse, fieldUserDto, QuestionnaireResponseStatus.INACTIVE));
    }

    @PatchMapping("/api/field/customers/{customerId}/questionnaires/*/responses/{responseId}/status")
    public QuestionnaireResponseDto updateQuestionnaireResponseStatus(@AuthenticationPrincipal FieldUserDto fieldUserDto,
                                                                      @PathVariable("customerId") Long customerId,
                                                                      @PathVariable("responseId") Long responseId,
                                                                      @RequestBody QuestionnaireResponseStatusUpdateDto request) {
        QuestionnaireResponse existingQuestionnaireResponse = getQuestionnaireResponseSafe(fieldUserDto, customerId, responseId);
        return dtoMapper.map(questionnaireResponseService.markQuestionnaireResponseStatus(existingQuestionnaireResponse, fieldUserDto, request.status()));
    }

    @GetMapping("/api/field/customers/{customerId}/questionnaires/*/responses/photoUploadUrl")
    public UploadUrlDto generateLeadUploadUrl(
            @AuthenticationPrincipal FieldUserDto fieldUserDto,
            @PathVariable("customerId") Long customerId,
            @RequestParam(name = "fileName") String fileName,
            @RequestParam(name = "contentType") String contentType) {
        Optional<Customer> customerOptional = customerService.getCustomerById(customerId, fieldUserDto);

        if(customerOptional.isEmpty()) {
            throw new RecordNotFoundException("Customer not found with customerId="+customerId);
        }

        Customer customer = customerOptional.get();

        log.info("Uploading photo with fileName={} for customerId={}", fileName, customer.getId());

        int validity = 300;
        LocalDateTime validUntil = LocalDateTime.now().plusSeconds(validity);
        String bucketKey = customerService.getBucketKeyForFileAndStage(customer, UploadFileType.RESPONSE, fileName, FileStage.RAW);

        URL url = s3Service.generateUploadUrl(S3Service.WAITQUE_UPLOAD_BUCKET, bucketKey, contentType, validity);

        return uploadUrlDtoMapper.map(url, bucketKey, validUntil);
    }

    @DeleteMapping("/api/field/customers/{customerId}/questionnaires/*/responses/photoUpload")
    public void deleteLeadPhotoUpload(
            @AuthenticationPrincipal FieldUserDto fieldUserDto,
            @PathVariable("customerId") Long customerId,
            @RequestParam("photoPath") String path) {
        Optional<Customer> customerOptional = customerService.getCustomerById(customerId, fieldUserDto);

        if(customerOptional.isEmpty()) {
            throw new RecordNotFoundException("Customer not found with customerId="+customerId);
        }

        Customer customer = customerOptional.get();

        log.info("Deleting photo with path={} for customerId={}", path, customer.getId());

        if(!path.contains("/" + customerId + "/")) {
            throw new IllegalRequestException("Cannot delete photo for another customer");
        }

        s3Service.delete(S3Service.WAITQUE_UPLOAD_BUCKET, path);
    }
}
