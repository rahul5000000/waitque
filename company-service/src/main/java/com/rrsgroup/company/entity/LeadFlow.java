package com.rrsgroup.company.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_flow", schema = "company")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeadFlow {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @NotBlank
    @Size(max = 512)
    private String status;
    @NotBlank
    @Size(max = 512)
    private String name;
    @NotBlank
    @Size(max = 2048)
    private String icon;
    @NotBlank
    @Size(max = 128)
    private String button_text;
    @NotBlank
    @Size(max = 128)
    private String title;
    @NotBlank
    @Size(max = 128)
    private String confirmation_message_header;
    @Size(max = 128)
    private String confirmation_message_1;
    @Size(max = 128)
    private String confirmation_message_2;
    @Size(max = 128)
    private String confirmation_message_3;
    @NotBlank
    private LocalDateTime created_date;
    @NotBlank
    private LocalDateTime updated_date;
    @NotBlank
    private String created_by;
    @NotBlank
    private String updated_by;
}
