package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.domain.lead.LeadStatus;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.entity.lead.LeadStatusCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    @Query("SELECT l FROM Lead l JOIN l.customer cust JOIN cust.crmConfig crmc WHERE crmc.companyId = :companyId")
    Page<Lead> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT l FROM Lead l JOIN l.customer cust JOIN cust.crmConfig crmc WHERE crmc.companyId = :companyId AND l.status IN (:statuses)")
    Page<Lead> findByCompanyIdAndStatusIn(@Param("companyId") Long companyId, @Param("statuses") Collection<LeadStatus> statuses, Pageable pageable);

    Optional<Lead> findByIdAndCustomer_CrmConfig_CompanyId(Long id, Long companyId);

    @Query("""
    SELECT
        SUM(CASE WHEN l.status = com.rrsgroup.customer.domain.lead.LeadStatus.NEW THEN 1 ELSE 0 END) AS newCount,
        SUM(CASE WHEN l.status IN (
            com.rrsgroup.customer.domain.lead.LeadStatus.IN_PROGRESS,
            com.rrsgroup.customer.domain.lead.LeadStatus.RECONTACT
        ) THEN 1 ELSE 0 END) AS inProgressCount
        FROM Lead l
        JOIN l.customer cust
        JOIN cust.crmConfig crmc
        WHERE crmc.companyId = :companyId
    """)
    LeadStatusCount countLeadsByStatusForCompanyId(@Param("companyId") Long companyId);

}
