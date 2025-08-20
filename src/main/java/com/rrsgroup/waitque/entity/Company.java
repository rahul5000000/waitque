package com.rrsgroup.waitque.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Company {
    @GeneratedValue
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
}
