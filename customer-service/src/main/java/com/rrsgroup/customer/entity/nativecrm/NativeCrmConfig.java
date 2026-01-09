package com.rrsgroup.customer.entity.nativecrm;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "crm_config", schema = "crm")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NativeCrmConfig {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private Long tenantId;

    @NotNull
    private Long companyId;
}
