package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.domain.lead.LeadStatus;
import com.rrsgroup.customer.domain.questionnaireresponse.QuestionnaireResponseStatus;
import com.rrsgroup.customer.entity.questionnaireresponse.QuestionnaireResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {
    @Query("SELECT qr FROM QuestionnaireResponse qr JOIN qr.customer cust JOIN cust.crmConfig crmc WHERE crmc.companyId = :companyId AND cust.id = :customerId")
    Page<QuestionnaireResponse> findByCustomerIdAndCompanyId(@Param("customerId") Long customerId, @Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT qr FROM QuestionnaireResponse qr JOIN qr.customer cust JOIN cust.crmConfig crmc WHERE crmc.companyId = :companyId AND qr.status IN (:statuses) AND cust.id = :customerId")
    Page<QuestionnaireResponse> findByCustomerIdAndCompanyIdAndStatusIn(@Param("customerId") Long customerId, @Param("companyId") Long companyId, @Param("statuses") Collection<QuestionnaireResponseStatus> statuses, Pageable pageable);

    Optional<QuestionnaireResponse> findByIdAndCustomer_IdAndCustomer_CrmConfig_CompanyId(Long id, Long customerId, Long companyId);
}
