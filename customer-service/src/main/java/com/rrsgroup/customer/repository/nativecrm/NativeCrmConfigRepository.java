package com.rrsgroup.customer.repository.nativecrm;

import com.rrsgroup.customer.entity.nativecrm.NativeCrmConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NativeCrmConfigRepository extends JpaRepository<NativeCrmConfig, Long> {
    Optional<NativeCrmConfig> findByCompanyId(Long companyId);

}
