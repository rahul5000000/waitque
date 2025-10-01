package com.rrsgroup.company.entity;

import com.rrsgroup.company.domain.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "qr_code", schema = "company")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QrCode {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @NotNull
    private UUID qrCode;
    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
