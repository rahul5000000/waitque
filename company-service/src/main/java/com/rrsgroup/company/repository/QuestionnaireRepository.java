package com.rrsgroup.company.repository;

import com.rrsgroup.company.entity.questionnaire.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
}
