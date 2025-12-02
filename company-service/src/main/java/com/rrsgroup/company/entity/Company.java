package com.rrsgroup.company.entity;

import com.rrsgroup.common.entity.Address;
import com.rrsgroup.common.entity.PhoneNumber;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company", schema = "company")
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

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompanyEmail> emails = new ArrayList<>();
}
