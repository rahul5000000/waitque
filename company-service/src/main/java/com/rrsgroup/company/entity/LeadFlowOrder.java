package com.rrsgroup.company.entity;

import com.rrsgroup.company.domain.LeadFlowStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_flow_id", referencedColumnName = "id", unique = true)
    private LeadFlow leadFlow;

    @NotNull
    @Min(0)
    private Integer ordinal;
    @NotNull
    @Enumerated(EnumType.STRING)
    private LeadFlowStatus status;
    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;

    public boolean updateRequiresNewRecord(LeadFlowOrder newVersion) {
        return !ordinal.equals(newVersion.getOrdinal());
    }
}
