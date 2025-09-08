package com.rrsgroup.company.entity;

import com.rrsgroup.company.domain.LeadFlowQuestionDataType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_flow_question", schema = "company")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeadFlowQuestion {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_flow_id", referencedColumnName = "id", unique = true)
    private LeadFlow leadFlow;

    @NotBlank
    @Size(max = 512)
    private String question;
    @NotBlank
    @Size(max = 512)
    private LeadFlowQuestionDataType dataType;
    @NotBlank
    private LocalDateTime createdDate;
    @NotBlank
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
