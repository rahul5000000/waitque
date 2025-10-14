package com.rrsgroup.customer.entity.lead;

import com.rrsgroup.customer.domain.LeadFlowQuestionDataType;
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
@Table(name = "lead_answer", schema = "customer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeadAnswer {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_id", referencedColumnName = "id")
    private Lead lead;

    @NotNull
    private Long leadFlowQuestionId;
    @NotNull
    @Enumerated(EnumType.STRING)
    private LeadFlowQuestionDataType dataType;
    private Boolean booleanAnswer;
    @Size(max = 512, message = "Answer text cannot exceed 500 characters")
    private String textAnswer;
    @Size(max = 2048, message = "Answer textarea cannot exceed 2048 characters")
    private String textAreaAnswer;
    @Size(max = 2048, message = "URL cannot exceed 2048 characters")
    private String imageUrl;
    private Long numberAnswer;
    private Double decimalAnswer;

    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
