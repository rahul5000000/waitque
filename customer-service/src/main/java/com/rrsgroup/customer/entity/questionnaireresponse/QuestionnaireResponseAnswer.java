package com.rrsgroup.customer.entity.questionnaireresponse;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
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
@Table(name = "questionnaire_response_answer", schema = "customer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionnaireResponseAnswer {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questionnaire_response_id", referencedColumnName = "id")
    private QuestionnaireResponse questionnaireResponse;

    @NotNull
    private Long questionnaireQuestionId;
    @NotNull
    @Enumerated(EnumType.STRING)
    private QuestionnaireQuestionDataType dataType;
    private Boolean booleanAnswer;
    @Size(max = 512, message = "Answer text cannot exceed 500 characters")
    private String textAnswer;
    @Size(max = 2048, message = "Answer textarea cannot exceed 2048 characters")
    private String textAreaAnswer;
    @Size(max = 2048, message = "URL cannot exceed 2048 characters")
    private String imageUrl;
    private Long numberAnswer;
    private Double decimalAnswer;
    private Long phoneAnswer;
    @Size(max = 256, message = "Answer text cannot exceed 256 characters")
    private String emailAnswer;

    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
