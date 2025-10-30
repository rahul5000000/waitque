package com.rrsgroup.company.entity.questionnaire;

import com.rrsgroup.company.domain.questionnaire.QuestionnaireQuestionDataType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "questionnaire_question", schema = "company")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionnaireQuestion {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questionnaire_page_id", referencedColumnName = "id")
    private QuestionnairePage questionnairePage;

    @NotBlank
    @Size(max = 512)
    private String question;

    @NotNull
    @Enumerated(EnumType.STRING)
    private QuestionnaireQuestionDataType dataType;
    @NotNull
    private Boolean isRequired;
    private String falseText;
    private String trueText;
    private String questionGroup;
    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
