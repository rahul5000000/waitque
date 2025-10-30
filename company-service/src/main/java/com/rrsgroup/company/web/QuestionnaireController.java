package com.rrsgroup.company.web;

import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.company.domain.questionnaire.QuestionnaireType;
import com.rrsgroup.company.dto.questionnaire.DefaultQuestionnaireRequestDto;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.service.CompanyService;
import com.rrsgroup.company.service.QuestionnaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class QuestionnaireController {
    private final CompanyService companyService;
    private final  QuestionnaireService questionnaireService;

    @Autowired
    public QuestionnaireController(CompanyService companyService, QuestionnaireService questionnaireService) {
        this.companyService = companyService;
        this.questionnaireService = questionnaireService;
    }

    @PostMapping("/api/internal/questionnaires/default")
    public void createDefaultQuestionnaire(@RequestBody DefaultQuestionnaireRequestDto request) {
        Company company = getCompanySafe(request.companyId());
        questionnaireService.createDefaultQuestionnaire(company, request.type());
    }

    private Company getCompanySafe(Long companyId) {
        Optional<Company> company = companyService.getCompany(companyId);

        if(company.isEmpty()) {
            throw new RecordNotFoundException("Company not found with that ID");
        }

        return company.get();
    }
}
