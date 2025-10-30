package com.rrsgroup.company.entity.questionnaire;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questionnaire_page", schema = "company")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionnairePage {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @NotBlank
    @Size(max = 512)
    private String pageTitle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questionnaire_id", referencedColumnName = "id")
    private Questionnaire questionnaire;

    @NotNull
    @Min(0)
    private Integer pageNumber;

    @OneToMany(mappedBy = "questionnairePage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuestionnaireQuestion> questions = new ArrayList<>();

    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
