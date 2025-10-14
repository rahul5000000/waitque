package com.rrsgroup.customer.entity.message;

import com.rrsgroup.common.entity.Address;
import com.rrsgroup.common.entity.PhoneNumber;
import com.rrsgroup.customer.domain.message.MessageStatus;
import com.rrsgroup.customer.entity.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message", schema = "customer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

    @NotBlank
    @Size(max = 2048)
    private String message;
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
