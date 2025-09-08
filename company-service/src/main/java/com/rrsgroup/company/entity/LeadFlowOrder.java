package com.rrsgroup.company.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_flow_order", schema = "company")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeadFlowOrder {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", referencedColumnName = "id", unique = true)
    private Company company;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_flow_id", referencedColumnName = "id", unique = true)
    private LeadFlow leadFlow;

    @NotBlank
    @Min(0)
    private Integer ordinal;
    @NotBlank
    private LocalDateTime createdDate;
    @NotBlank
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
