package com.rrsgroup.company.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lead_flow", schema = "company")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeadFlow {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @NotBlank
    @Size(max = 512)
    private String name;
    @NotBlank
    @Size(max = 2048)
    private String icon;
    @NotBlank
    @Size(max = 128)
    private String buttonText;
    @NotBlank
    @Size(max = 128)
    private String title;
    @NotBlank
    @Size(max = 128)
    private String confirmationMessageHeader;
    @Size(max = 128)
    private String confirmationMessage1;
    @Size(max = 128)
    private String confirmationMessage2;
    @Size(max = 128)
    private String confirmationMessage3;
    @NotNull
    private LocalDateTime createdDate;
    @NotNull
    private LocalDateTime updatedDate;
    @NotBlank
    private String createdBy;
    @NotBlank
    private String updatedBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predecessor_id")
    private LeadFlow predecessor;

    @OneToMany(mappedBy = "leadFlow", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LeadFlowQuestion> questions = new ArrayList<>();

    @OneToOne(mappedBy = "leadFlow", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LeadFlowOrder leadFlowOrder;
}
