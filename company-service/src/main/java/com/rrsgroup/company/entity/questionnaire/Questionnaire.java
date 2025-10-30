package com.rrsgroup.company.entity.questionnaire;

import com.rrsgroup.company.domain.questionnaire.QuestionnaireStatus;
import com.rrsgroup.company.entity.Company;
import jakarta.persistence.*;
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
@Table(name = "questionnaire", schema = "company")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Questionnaire {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @NotBlank
    @Size(max = 512)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @NotBlank
    @Size(max = 2056)
    private String description;
    @NotNull
    @Enumerated(EnumType.STRING)
    private QuestionnaireStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predecessor_id")
    private Questionnaire predecessor;

    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuestionnairePage> pages = new ArrayList<>();

    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
