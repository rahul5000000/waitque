package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.entity.CrmConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmConfigRepository extends JpaRepository<CrmConfig, Long> {
    public List<CrmConfig> findByCompanyId(Long companyId);
}
