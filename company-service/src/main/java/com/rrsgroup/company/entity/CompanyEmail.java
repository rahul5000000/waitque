package com.rrsgroup.company.entity;

import com.rrsgroup.common.entity.Email;
import com.rrsgroup.company.domain.EmailStatus;
import com.rrsgroup.company.domain.EmailType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_email", schema = "company")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class CompanyEmail {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private EmailType type;

    @Enumerated(EnumType.STRING)
    private EmailStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "email_id", nullable = false)
    private Email email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    private Company company;
}
