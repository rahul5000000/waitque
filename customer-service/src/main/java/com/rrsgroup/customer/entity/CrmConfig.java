package com.rrsgroup.customer.entity;

import com.rrsgroup.customer.domain.CrmType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "crm_config", schema = "customer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrmConfig {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @NotNull
    private Long companyId;
    @NotNull
    private CrmType crmType;
    @NotBlank
    private String crmName;
    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
