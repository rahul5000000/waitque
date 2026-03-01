package com.rrsgroup.customer.entity.nativecrm;

import com.rrsgroup.common.entity.PhoneNumber;
import com.rrsgroup.customer.entity.questionnaireresponse.QuestionnaireResponse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_phone_number", schema = "crm")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NativeCrmPhoneNumber {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private String type;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phone_number_id", referencedColumnName = "id", unique = true)
    private PhoneNumber phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crm_customer_id", referencedColumnName = "id")
    private NativeCrmCustomer customer;

    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
