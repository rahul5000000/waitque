package com.rrsgroup.customer.entity.nativecrm;

import com.rrsgroup.common.entity.Address;
import com.rrsgroup.common.entity.Email;
import com.rrsgroup.common.entity.PhoneNumber;
import com.rrsgroup.customer.domain.CrmCustomerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer", schema = "crm")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NativeCrmCustomer {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private Long tenantId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CrmCustomerType customerType;
    private String companyName;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", referencedColumnName = "id", unique = true)
    private Address address;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phone_number_id", referencedColumnName = "id", unique = true)
    private PhoneNumber phoneNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "email_id", nullable = false)
    private Email email;

    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
