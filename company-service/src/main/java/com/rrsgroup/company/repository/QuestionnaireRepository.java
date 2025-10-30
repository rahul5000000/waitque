package com.rrsgroup.company.repository;

import com.rrsgroup.company.domain.questionnaire.QuestionnaireStatus;
import com.rrsgroup.company.entity.questionnaire.Questionnaire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    Page<Questionnaire> findByCompanyIdIn(List<Long> companyIds, Pageable pageable);
    Page<Questionnaire> findByCompanyIdInAndStatusIn(List<Long> companyIds, List<QuestionnaireStatus> statuses, Pageable pageable);
    Optional<Questionnaire> findByIdAndCompanyId(Long id, Long companyId);
}
