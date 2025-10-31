package com.rrsgroup.customer.entity.questionnaireresponse;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireStatus;
import com.rrsgroup.customer.domain.questionnaireresponse.QuestionnaireResponseStatus;
import com.rrsgroup.customer.entity.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questionnaire_response", schema = "customer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionnaireResponse {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private Long questionnaireId;
    @NotNull
    @Enumerated(EnumType.STRING)
    private QuestionnaireResponseStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "questionnaireResponse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuestionnaireResponseAnswer> answers = new ArrayList<>();

    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
