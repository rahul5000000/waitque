package com.rrsgroup.company.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.UserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.company.domain.questionnaire.QuestionnaireQuestionDataType;
import com.rrsgroup.company.domain.questionnaire.QuestionnaireStatus;
import com.rrsgroup.company.domain.questionnaire.QuestionnaireType;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.entity.questionnaire.Questionnaire;
import com.rrsgroup.company.entity.questionnaire.QuestionnairePage;
import com.rrsgroup.company.entity.questionnaire.QuestionnaireQuestion;
import com.rrsgroup.company.repository.QuestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionnaireService {
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    public QuestionnaireService(QuestionnaireRepository questionnaireRepository) {
        this.questionnaireRepository = questionnaireRepository;
    }

    public Questionnaire createDefaultQuestionnaire(Company company, QuestionnaireType type, UserDto createdBy) {
        return switch (type) {
            case DEFAULT_RESTORATION -> createDefaultRestorationQuestionnaire(company, createdBy);
            default -> throw new IllegalRequestException("This method can only create default questionnaire types");
        };
    }

    private Questionnaire createDefaultRestorationQuestionnaire(Company company, UserDto createdByUser) {
        LocalDateTime now = LocalDateTime.now();
        String createdBy = createdByUser.getUserId();
        Questionnaire questionnaire = Questionnaire.builder()
                .name("Business Emergency Plan")
                .company(company)
                .status(QuestionnaireStatus.ACTIVE)
                .description("Automatically created Business Emergency Plan questionnaire for restoration firms")
                .createdDate(now)
                .updatedDate(now)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        int pageNumber = 0;
        QuestionnairePage customerInfoPage = createPage("Customer Info", ++pageNumber, now, createdBy, questionnaire);

        customerInfoPage.setQuestions(List.of(
                createQuestion("Customer Name", QuestionnaireQuestionDataType.TEXT, false, customerInfoPage, now, createdBy),
                createQuestion("Customer Logo", QuestionnaireQuestionDataType.IMAGE, false, customerInfoPage, now, createdBy)
        ));

        QuestionnairePage propertyDetailsPage = createPage("Property Details", ++pageNumber, now, createdBy, questionnaire);

        propertyDetailsPage.setQuestions(List.of(
                createQuestion("Photos of Building", QuestionnaireQuestionDataType.IMAGE, false, propertyDetailsPage, now, createdBy),
                createQuestion("Year Built", QuestionnaireQuestionDataType.NUMBER, false, propertyDetailsPage, now, createdBy),
                createQuestion("Year Renovated", QuestionnaireQuestionDataType.NUMBER, false, propertyDetailsPage, now, createdBy),
                createBooleanQuestion("Is Multi-Tenant?", QuestionnaireQuestionDataType.BOOLEAN, false, "No", "Yes", propertyDetailsPage, now, createdBy),
                createQuestion("Total Area (sqft)", QuestionnaireQuestionDataType.NUMBER, false, propertyDetailsPage, now, createdBy),
                createQuestion("Number of Floors", QuestionnaireQuestionDataType.NUMBER, false, propertyDetailsPage, now, createdBy),
                createQuestion("Number of Buildings", QuestionnaireQuestionDataType.NUMBER, false, propertyDetailsPage, now, createdBy),
                createQuestion("Type of Structure (Wood, Concrete, Metal, etc.)", QuestionnaireQuestionDataType.TEXT, false, propertyDetailsPage, now, createdBy)
        ));

        QuestionnairePage buildingAccessPage = createPage("Building Access", ++pageNumber, now, createdBy, questionnaire);

        buildingAccessPage.setQuestions(List.of(
                createQuestion("Location to Park", QuestionnaireQuestionDataType.TEXT, false, buildingAccessPage, now, createdBy),
                createQuestion("Location to Park", QuestionnaireQuestionDataType.IMAGE, false, buildingAccessPage, now, createdBy),
                createQuestion("Preferred Entrance", QuestionnaireQuestionDataType.TEXT, false, buildingAccessPage, now, createdBy),
                createQuestion("Preferred Entrance", QuestionnaireQuestionDataType.IMAGE, false, buildingAccessPage, now, createdBy),
                createQuestion("Elevator Access", QuestionnaireQuestionDataType.TEXT, false, buildingAccessPage, now, createdBy),
                createQuestion("Elevator Access", QuestionnaireQuestionDataType.IMAGE, false, buildingAccessPage, now, createdBy),
                createQuestion("Stairway Access", QuestionnaireQuestionDataType.TEXT, false, buildingAccessPage, now, createdBy),
                createQuestion("Stairway Access", QuestionnaireQuestionDataType.IMAGE, false, buildingAccessPage, now, createdBy),
                createQuestion("Special Instructions", QuestionnaireQuestionDataType.TEXTAREA, false, buildingAccessPage, now, createdBy)
        ));

        QuestionnairePage utilitiesPage = createPage("Utilities", ++pageNumber, now, createdBy, questionnaire);

        utilitiesPage.setQuestions(List.of(
                createGroupedQuestion("Company Name", QuestionnaireQuestionDataType.TEXT, false, "Electric", utilitiesPage, now, createdBy),
                createGroupedQuestion("Contact Name", QuestionnaireQuestionDataType.TEXT, false, "Electric", utilitiesPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Electric", utilitiesPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Electric", utilitiesPage, now, createdBy),
                createGroupedQuestion("Shut Off Location", QuestionnaireQuestionDataType.TEXT, false, "Electric", utilitiesPage, now, createdBy),
                createGroupedQuestion("Shut Off Location", QuestionnaireQuestionDataType.IMAGE, false, "Electric", utilitiesPage, now, createdBy),
                createGroupedBooleanQuestion("Shut Off Key Required?", QuestionnaireQuestionDataType.BOOLEAN, false, "No", "Yes", "Electric", utilitiesPage, now, createdBy),
                createGroupedQuestion("Utilities Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Electric", utilitiesPage, now, createdBy),
                createGroupedQuestion("Company Name", QuestionnaireQuestionDataType.TEXT, false, "Gas", utilitiesPage, now, createdBy),
                createGroupedQuestion("Contact Name", QuestionnaireQuestionDataType.TEXT, false, "Gas", utilitiesPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Gas", utilitiesPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Gas", utilitiesPage, now, createdBy),
                createGroupedQuestion("Shut Off Location", QuestionnaireQuestionDataType.TEXT, false, "Gas", utilitiesPage, now, createdBy),
                createGroupedQuestion("Shut Off Location", QuestionnaireQuestionDataType.IMAGE, false, "Gas", utilitiesPage, now, createdBy),
                createGroupedBooleanQuestion("Shut Off Key Required?", QuestionnaireQuestionDataType.BOOLEAN, false, "No", "Yes", "Gas", utilitiesPage, now, createdBy),
                createGroupedQuestion("Utilities Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Gas", utilitiesPage, now, createdBy),
                createGroupedQuestion("Company Name", QuestionnaireQuestionDataType.TEXT, false, "Water", utilitiesPage, now, createdBy),
                createGroupedQuestion("Contact Name", QuestionnaireQuestionDataType.TEXT, false, "Water", utilitiesPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Water", utilitiesPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Water", utilitiesPage, now, createdBy),
                createGroupedQuestion("Shut Off Location", QuestionnaireQuestionDataType.TEXT, false, "Water", utilitiesPage, now, createdBy),
                createGroupedQuestion("Shut Off Location", QuestionnaireQuestionDataType.IMAGE, false, "Water", utilitiesPage, now, createdBy),
                createGroupedBooleanQuestion("Shut Off Key Required?", QuestionnaireQuestionDataType.BOOLEAN, false, "No", "Yes", "Water", utilitiesPage, now, createdBy),
                createGroupedQuestion("Utilities Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Water", utilitiesPage, now, createdBy),
                createGroupedQuestion("Contact Name", QuestionnaireQuestionDataType.TEXT, false, "Alarm", utilitiesPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Alarm", utilitiesPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Alarm", utilitiesPage, now, createdBy),
                createGroupedQuestion("Shut Off Location", QuestionnaireQuestionDataType.TEXT, false, "Alarm", utilitiesPage, now, createdBy),
                createGroupedQuestion("Shut Off Location", QuestionnaireQuestionDataType.IMAGE, false, "Alarm", utilitiesPage, now, createdBy),
                createGroupedBooleanQuestion("Shut Off Key Required?", QuestionnaireQuestionDataType.BOOLEAN, false, "No", "Yes", "Alarm", utilitiesPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Alarm", utilitiesPage, now, createdBy),
                createGroupedQuestion("Company Name", QuestionnaireQuestionDataType.TEXT, false, "Sprinkler", utilitiesPage, now, createdBy),
                createGroupedQuestion("Contact Name", QuestionnaireQuestionDataType.TEXT, false, "Sprinkler", utilitiesPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Sprinkler", utilitiesPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Sprinkler", utilitiesPage, now, createdBy),
                createGroupedQuestion("Shut Off Location", QuestionnaireQuestionDataType.TEXT, false, "Sprinkler", utilitiesPage, now, createdBy),
                createGroupedQuestion("Shut Off Location", QuestionnaireQuestionDataType.IMAGE, false, "Sprinkler", utilitiesPage, now, createdBy),
                createGroupedBooleanQuestion("Shut Off Key Required?", QuestionnaireQuestionDataType.BOOLEAN, false, "No", "Yes", "Sprinkler", utilitiesPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Sprinkler", utilitiesPage, now, createdBy),
                createGroupedQuestion("Company Name", QuestionnaireQuestionDataType.TEXT, false, "IT/Server", utilitiesPage, now, createdBy),
                createGroupedQuestion("Contact Name", QuestionnaireQuestionDataType.TEXT, false, "IT/Server", utilitiesPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "IT/Server", utilitiesPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "IT/Server", utilitiesPage, now, createdBy),
                createGroupedQuestion("Location", QuestionnaireQuestionDataType.TEXT, false, "IT/Server", utilitiesPage, now, createdBy),
                createGroupedQuestion("Location", QuestionnaireQuestionDataType.IMAGE, false, "IT/Server", utilitiesPage, now, createdBy),
                createGroupedBooleanQuestion("Access Key Required?", QuestionnaireQuestionDataType.BOOLEAN, false, "No", "Yes", "IT/Server", utilitiesPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "IT/Server", utilitiesPage, now, createdBy)
        ));

        QuestionnairePage keyContactsPage = createPage("Key Contacts & Vendors", ++pageNumber, now, createdBy, questionnaire);

        keyContactsPage.setQuestions(List.of(
                createGroupedQuestion("Name", QuestionnaireQuestionDataType.TEXT, false, "Building Owner", keyContactsPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Building Owner", keyContactsPage, now, createdBy),
                createGroupedQuestion("Alternate Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Building Owner", keyContactsPage, now, createdBy),
                createGroupedQuestion("Email Address", QuestionnaireQuestionDataType.EMAIL, false, "Building Owner", keyContactsPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Building Owner", keyContactsPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Building Owner", keyContactsPage, now, createdBy),
                createGroupedQuestion("Name", QuestionnaireQuestionDataType.TEXT, false, "HVAC", keyContactsPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "HVAC", keyContactsPage, now, createdBy),
                createGroupedQuestion("Alternate Phone Number", QuestionnaireQuestionDataType.PHONE, false, "HVAC", keyContactsPage, now, createdBy),
                createGroupedQuestion("Email Address", QuestionnaireQuestionDataType.EMAIL, false, "HVAC", keyContactsPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "HVAC", keyContactsPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "HVAC", keyContactsPage, now, createdBy),
                createGroupedQuestion("Name", QuestionnaireQuestionDataType.TEXT, false, "Insurance", keyContactsPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Insurance", keyContactsPage, now, createdBy),
                createGroupedQuestion("Alternate Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Insurance", keyContactsPage, now, createdBy),
                createGroupedQuestion("Email Address", QuestionnaireQuestionDataType.EMAIL, false, "Insurance", keyContactsPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Insurance", keyContactsPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Insurance", keyContactsPage, now, createdBy),
                createGroupedQuestion("Name", QuestionnaireQuestionDataType.TEXT, false, "Local Fire", keyContactsPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Local Fire", keyContactsPage, now, createdBy),
                createGroupedQuestion("Alternate Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Local Fire", keyContactsPage, now, createdBy),
                createGroupedQuestion("Email Address", QuestionnaireQuestionDataType.EMAIL, false, "Local Fire", keyContactsPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Local Fire", keyContactsPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Local Fire", keyContactsPage, now, createdBy),
                createGroupedQuestion("Name", QuestionnaireQuestionDataType.TEXT, false, "Local Police", keyContactsPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Local Police", keyContactsPage, now, createdBy),
                createGroupedQuestion("Alternate Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Local Police", keyContactsPage, now, createdBy),
                createGroupedQuestion("Email Address", QuestionnaireQuestionDataType.EMAIL, false, "Local Police", keyContactsPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Local Police", keyContactsPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Local Police", keyContactsPage, now, createdBy),
                createGroupedQuestion("Name", QuestionnaireQuestionDataType.TEXT, false, "Local Hospital", keyContactsPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Local Hospital", keyContactsPage, now, createdBy),
                createGroupedQuestion("Alternate Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Local Hospital", keyContactsPage, now, createdBy),
                createGroupedQuestion("Email Address", QuestionnaireQuestionDataType.EMAIL, false, "Local Hospital", keyContactsPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Local Hospital", keyContactsPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Local Hospital", keyContactsPage, now, createdBy),
                createGroupedQuestion("Name", QuestionnaireQuestionDataType.TEXT, false, "HR", keyContactsPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "HR", keyContactsPage, now, createdBy),
                createGroupedQuestion("Alternate Phone Number", QuestionnaireQuestionDataType.PHONE, false, "HR", keyContactsPage, now, createdBy),
                createGroupedQuestion("Email Address", QuestionnaireQuestionDataType.EMAIL, false, "HR", keyContactsPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "HR", keyContactsPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "HR", keyContactsPage, now, createdBy),
                createGroupedQuestion("Name", QuestionnaireQuestionDataType.TEXT, false, "Facilities Management", keyContactsPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Facilities Management", keyContactsPage, now, createdBy),
                createGroupedQuestion("Alternate Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Facilities Management", keyContactsPage, now, createdBy),
                createGroupedQuestion("Email Address", QuestionnaireQuestionDataType.EMAIL, false, "Facilities Management", keyContactsPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Facilities Management", keyContactsPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Facilities Management", keyContactsPage, now, createdBy),
                createGroupedQuestion("Name", QuestionnaireQuestionDataType.TEXT, false, "Onsite", keyContactsPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Onsite", keyContactsPage, now, createdBy),
                createGroupedQuestion("Alternate Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Onsite", keyContactsPage, now, createdBy),
                createGroupedQuestion("Email Address", QuestionnaireQuestionDataType.EMAIL, false, "Onsite", keyContactsPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Onsite", keyContactsPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Onsite", keyContactsPage, now, createdBy),
                createGroupedQuestion("Name", QuestionnaireQuestionDataType.TEXT, false, "Property Manager", keyContactsPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Property Manager", keyContactsPage, now, createdBy),
                createGroupedQuestion("Alternate Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Property Manager", keyContactsPage, now, createdBy),
                createGroupedQuestion("Email Address", QuestionnaireQuestionDataType.EMAIL, false, "Property Manager", keyContactsPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Property Manager", keyContactsPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Property Manager", keyContactsPage, now, createdBy),
                createGroupedQuestion("Name", QuestionnaireQuestionDataType.TEXT, false, "Security", keyContactsPage, now, createdBy),
                createGroupedQuestion("Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Security", keyContactsPage, now, createdBy),
                createGroupedQuestion("Alternate Phone Number", QuestionnaireQuestionDataType.PHONE, false, "Security", keyContactsPage, now, createdBy),
                createGroupedQuestion("Email Address", QuestionnaireQuestionDataType.EMAIL, false, "Security", keyContactsPage, now, createdBy),
                createGroupedQuestion("Account Number", QuestionnaireQuestionDataType.TEXT, false, "Security", keyContactsPage, now, createdBy),
                createGroupedQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, "Security", keyContactsPage, now, createdBy)
        ));

        QuestionnairePage workAuthPage = createPage("Work Authorization", ++pageNumber, now, createdBy, questionnaire);

        workAuthPage.setQuestions(List.of(
                createQuestion("Authorizer Name", QuestionnaireQuestionDataType.TEXT, false, workAuthPage, now, createdBy),
                createQuestion("Authorizer Phone Number", QuestionnaireQuestionDataType.PHONE, false, workAuthPage, now, createdBy),
                createQuestion("Authorizer Email", QuestionnaireQuestionDataType.EMAIL, false, workAuthPage, now, createdBy),
                createQuestion("Pre-authorized Amount", QuestionnaireQuestionDataType.NUMBER, false, workAuthPage, now, createdBy)
        ));

        QuestionnairePage notesPage = createPage("Notes", ++pageNumber, now, createdBy, questionnaire);

        notesPage.setQuestions(List.of(
                createQuestion("Notes", QuestionnaireQuestionDataType.TEXTAREA, false, notesPage, now, createdBy)
        ));

        questionnaire.setPages(List.of(customerInfoPage, propertyDetailsPage, buildingAccessPage, utilitiesPage, keyContactsPage, workAuthPage, notesPage));

        return questionnaireRepository.save(questionnaire);
    }

    private QuestionnairePage createPage(String title, int pageNumber, LocalDateTime now, String createdBy, Questionnaire questionnaire) {
        return QuestionnairePage.builder()
                .pageTitle(title)
                .pageNumber(pageNumber)
                .createdDate(now)
                .updatedDate(now)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .questionnaire(questionnaire)
                .build();
    }

    private QuestionnaireQuestion createQuestion(String question, QuestionnaireQuestionDataType type, boolean isRequired, QuestionnairePage page, LocalDateTime now, String createdBy) {
        if(type == QuestionnaireQuestionDataType.BOOLEAN) throw new IllegalRequestException("Cannot create a non-boolean question for a boolean type");
        return QuestionnaireQuestion.builder()
                .question(question)
                .dataType(type)
                .isRequired(isRequired)
                .questionnairePage(page)
                .createdDate(now)
                .updatedDate(now)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();
    }

    private QuestionnaireQuestion createGroupedQuestion(String question, QuestionnaireQuestionDataType type, boolean isRequired, String group, QuestionnairePage page, LocalDateTime now, String createdBy) {
        if(type == QuestionnaireQuestionDataType.BOOLEAN) throw new IllegalRequestException("Cannot create a non-boolean question for a boolean type");
        return QuestionnaireQuestion.builder()
                .question(question)
                .dataType(type)
                .isRequired(isRequired)
                .questionGroup(group)
                .questionnairePage(page)
                .createdDate(now)
                .updatedDate(now)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();
    }

    private QuestionnaireQuestion createGroupedBooleanQuestion(String question, QuestionnaireQuestionDataType type, boolean isRequired, String falseText, String trueText, String group, QuestionnairePage page, LocalDateTime now, String createdBy) {
        if(type != QuestionnaireQuestionDataType.BOOLEAN) throw new IllegalRequestException("Cannot create a boolean question for non-boolean type");
        return QuestionnaireQuestion.builder()
                .question(question)
                .dataType(type)
                .isRequired(isRequired)
                .falseText(falseText)
                .trueText(trueText)
                .questionGroup(group)
                .questionnairePage(page)
                .createdDate(now)
                .updatedDate(now)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();
    }

    private QuestionnaireQuestion createBooleanQuestion(String question, QuestionnaireQuestionDataType type, boolean isRequired, String falseText, String trueText, QuestionnairePage page, LocalDateTime now, String createdBy) {
        if(type != QuestionnaireQuestionDataType.BOOLEAN) throw new IllegalRequestException("Cannot create a boolean question for non-boolean type");
        return QuestionnaireQuestion.builder()
                .question(question)
                .dataType(type)
                .isRequired(isRequired)
                .falseText(falseText)
                .trueText(trueText)
                .questionnairePage(page)
                .createdDate(now)
                .updatedDate(now)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();
    }

    public Page<Questionnaire> getListOfQuestionnaires(List<Long> companyIds, List<QuestionnaireStatus> statuses, int limit, int page, String sortField, SortDirection sortDir) {
        Pageable pageable = PageRequest.of(
                page,
                limit,
                sortDir == SortDirection.ASC ? Sort.by(sortField).ascending() : Sort.by(sortField).descending());

        if(statuses == null || statuses.isEmpty()) {
            return questionnaireRepository.findByCompanyIdIn(companyIds, pageable);
        } else {
            return questionnaireRepository.findByCompanyIdInAndStatusIn(companyIds, statuses, pageable);
        }
    }

    public Optional<Questionnaire> getQuestionnaireById(Long questionnaireId, Long companyId) {
        return questionnaireRepository.findByIdAndCompanyId(questionnaireId, companyId);
    }

    public Questionnaire inactivateQuestionnaire(Long questionnaireId, Long companyId, UserDto updatedBy) {
        Optional<Questionnaire> questionnaireOptional = getQuestionnaireById(questionnaireId, companyId);

        if(questionnaireOptional.isEmpty()) {
            throw new RecordNotFoundException("Questionnaire not found with id=" + questionnaireId);
        }

        LocalDateTime updatedDate = LocalDateTime.now();
        String updatedByUserId = updatedBy.getUserId();

        Questionnaire questionnaire = questionnaireOptional.get();
        questionnaire.setStatus(QuestionnaireStatus.INACTIVE);
        questionnaire.setUpdatedBy(updatedByUserId);
        questionnaire.setUpdatedDate(updatedDate);

        return questionnaireRepository.save(questionnaire);
    }
}
