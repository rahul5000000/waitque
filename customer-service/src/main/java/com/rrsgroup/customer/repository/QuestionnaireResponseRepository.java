package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.entity.questionnaireresponse.QuestionnaireResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {
}
