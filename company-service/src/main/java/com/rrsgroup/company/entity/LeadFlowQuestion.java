package com.rrsgroup.company.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_flow_question", schema = "company")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeadFlowQuestion {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_flow_id", referencedColumnName = "id", unique = true)
    private LeadFlow leadFlow;

    @NotBlank
    @Size(max = 512)
    private String question;
    @NotBlank
    @Size(max = 512)
    private String data_type;
    @NotBlank
    private LocalDateTime created_date;
    @NotBlank
    private LocalDateTime updated_date;
    @NotBlank
    private String created_by;
    @NotBlank
    private String updated_by;
}
