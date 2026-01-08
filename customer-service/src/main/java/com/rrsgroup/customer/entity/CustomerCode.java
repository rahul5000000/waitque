package com.rrsgroup.customer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_code", schema = "customer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerCode {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @NotNull
    private String customerCode;
    @NotNull
    @Enumerated(EnumType.STRING)
    private CustomerCodeStatus status;

    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;

    public enum CustomerCodeStatus {
        ACTIVE,
        INACTIVE
    }
}