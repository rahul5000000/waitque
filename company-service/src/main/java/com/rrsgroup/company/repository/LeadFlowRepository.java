package com.rrsgroup.company.repository;

import com.rrsgroup.company.domain.Status;
import com.rrsgroup.company.entity.LeadFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface LeadFlowRepository extends JpaRepository<LeadFlow, Long> {
    @Query("SELECT lf FROM LeadFlow lf JOIN lf.leadFlowOrder lfo JOIN lfo.company c WHERE c.id = :companyId")
    Page<LeadFlow> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT lf FROM LeadFlow lf JOIN lf.leadFlowOrder lfo JOIN lfo.company c WHERE c.id = :companyId AND lfo.status IN (:statuses)")
    Page<LeadFlow> findByCompanyIdAndStatusIn(@Param("companyId") Long companyId, @Param("statuses") Collection<Status> statuses, Pageable pageable);

    @Query("SELECT lf FROM LeadFlow lf JOIN lf.leadFlowOrder lfo JOIN lfo.company c WHERE c.id = :companyId AND lf.id = :id")
    LeadFlow findByIdAndCompanyId(@Param("id") Long id, @Param("companyId") Long companyId);
}
