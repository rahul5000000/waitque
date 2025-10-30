package com.rrsgroup.company.web;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.common.dto.SuperUserDto;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.company.domain.questionnaire.QuestionnaireStatus;
import com.rrsgroup.company.dto.questionnaire.DefaultQuestionnaireRequestDto;
import com.rrsgroup.company.dto.questionnaire.QuestionnaireDto;
import com.rrsgroup.company.dto.questionnaire.QuestionnaireListDto;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.entity.questionnaire.Questionnaire;
import com.rrsgroup.company.service.CompanyService;
import com.rrsgroup.company.service.QuestionnaireDtoMapper;
import com.rrsgroup.company.service.QuestionnaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class QuestionnaireController {
    private final CompanyService companyService;
    private final QuestionnaireService questionnaireService;
    private final QuestionnaireDtoMapper questionnaireDtoMapper;

    @Autowired
    public QuestionnaireController(
            CompanyService companyService,
            QuestionnaireService questionnaireService,
            QuestionnaireDtoMapper questionnaireDtoMapper) {
        this.companyService = companyService;
        this.questionnaireService = questionnaireService;
        this.questionnaireDtoMapper = questionnaireDtoMapper;
    }

    @PostMapping("/api/internal/companies/{companyId}/questionnaires/default")
    public QuestionnaireDto createDefaultQuestionnaire(
            @AuthenticationPrincipal SuperUserDto user,
            @PathVariable("companyId") Long companyId,
            @RequestBody DefaultQuestionnaireRequestDto request) {
        Company company = getCompanySafe(companyId);
        return questionnaireDtoMapper.map(questionnaireService.createDefaultQuestionnaire(company, request.type(), user));
    }

    @GetMapping("/api/internal/questionnaires")
    public QuestionnaireListDto superUserGetListOfQuestionnaires(
            @RequestParam(name = "status", required = false) List<QuestionnaireStatus> statuses,
            @RequestParam(name = "companyId") List<Long> companyIds,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "sortField", required = false, defaultValue = "companyId") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") SortDirection sortDir
    ) {
        Page<Questionnaire> questionnairePage = questionnaireService.getListOfQuestionnaires(companyIds, statuses, limit, page, sortField, sortDir);
        return questionnaireDtoMapper.map(questionnairePage);
    }

    @GetMapping("/api/internal/companies/{companyId}/questionnaires/{questionnaireId}")
    public QuestionnaireDto superUserGetQuestionnaire(
            @PathVariable("companyId") Long companyId,
            @PathVariable("questionnaireId") Long questionnaireId) {
        Optional<Questionnaire> questionnaireOptional = questionnaireService.getQuestionnaireById(questionnaireId, companyId);

        if(questionnaireOptional.isEmpty()) {
            throw new RecordNotFoundException("Questionnaire not found with id=" + questionnaireId);
        }

        return questionnaireDtoMapper.map(questionnaireOptional.get());
    }

    @DeleteMapping("/api/internal/companies/{companyId}/questionnaires/{questionnaireId}")
    public QuestionnaireDto superUserDeleteQuestionnaire(
            @AuthenticationPrincipal SuperUserDto user,
            @PathVariable("companyId") Long companyId,
            @PathVariable("questionnaireId") Long questionnaireId) {
        Questionnaire deletedQuestionnaire = questionnaireService.inactivateQuestionnaire(questionnaireId, companyId, user);
        return questionnaireDtoMapper.map(deletedQuestionnaire);
    }

    private Company getCompanySafe(Long companyId) {
        Optional<Company> company = companyService.getCompany(companyId);

        if(company.isEmpty()) {
            throw new RecordNotFoundException("Company not found with that ID");
        }

        return company.get();
    }

    @GetMapping({"/api/admin/questionnaires", "/api/field/questionnaires"})
    public QuestionnaireListDto getListOfQuestionnaires(
            @AuthenticationPrincipal CompanyUserDto user,
            @RequestParam(name = "status", required = false) List<QuestionnaireStatus> statuses,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "sortField", required = false, defaultValue = "companyId") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") SortDirection sortDir
    ) {
        Long companyId = user.getCompanyId();
        Page<Questionnaire> questionnairePage = questionnaireService.getListOfQuestionnaires(List.of(companyId), statuses, limit, page, sortField, sortDir);
        return questionnaireDtoMapper.map(questionnairePage);
    }

    @GetMapping({"/api/admin/questionnaires/{questionnaireId}", "/api/field/questionnaires/{questionnaireId}"})
    public QuestionnaireDto getQuestionnaire(
            @AuthenticationPrincipal CompanyUserDto user,
            @PathVariable("questionnaireId") Long questionnaireId) {
        Long companyId = user.getCompanyId();
        Optional<Questionnaire> questionnaireOptional = questionnaireService.getQuestionnaireById(questionnaireId, companyId);

        if(questionnaireOptional.isEmpty()) {
            throw new RecordNotFoundException("Questionnaire not found with id=" + questionnaireId);
        }

        return questionnaireDtoMapper.map(questionnaireOptional.get());
    }
}
