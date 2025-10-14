package com.rrsgroup.customer.entity.lead;

import com.rrsgroup.common.entity.Address;
import com.rrsgroup.common.entity.PhoneNumber;
import com.rrsgroup.customer.domain.lead.LeadStatus;
import com.rrsgroup.customer.entity.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lead", schema = "customer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Lead {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private Long leadFlowId;
    @NotNull
    private LeadStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LeadAnswer> answers = new ArrayList<>();

    private String overrideFirstName;
    private String overrideLastName;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "override_address_id", referencedColumnName = "id", unique = true)
    private Address overrideAddress;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "override_phone_number_id", referencedColumnName = "id", unique = true)
    private PhoneNumber overridePhoneNumber;

    private String overrideEmail;

    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;
}
