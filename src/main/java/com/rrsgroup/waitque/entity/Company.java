package com.rrsgroup.waitque.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Company {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String name;
    private String logoUrl;
    private String landingPrompt;
    private String textColor;
    private String backgroundColor;
    private String primaryButtonColor;
    private String secondaryButtonColor;
    private String warningButtonColor;
    private String dangerButtonColor;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", referencedColumnName = "id", unique = true)
    private Address address;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "phone_number_id", referencedColumnName = "id", unique = true)
    private PhoneNumber phoneNumber;
}
