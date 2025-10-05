package com.rrsgroup.customer.service;

import com.rrsgroup.common.dto.UserDto;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.customer.domain.CrmType;
import com.rrsgroup.customer.entity.CrmConfig;
import com.rrsgroup.customer.repository.CrmConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CrmConfigService {
    private final CrmConfigRepository crmConfigRepository;

    @Autowired
    public CrmConfigService(CrmConfigRepository crmConfigRepository) {
        this.crmConfigRepository = crmConfigRepository;
    }

    public CrmConfig addCrmConfig(CrmConfig config, UserDto createdBy) {
        LocalDateTime now = LocalDateTime.now();
        String userId = createdBy.getUserId();

        config.setCreatedBy(userId);
        config.setCreatedDate(now);
        config.setUpdatedBy(userId);
        config.setUpdatedDate(now);

        try {
            return crmConfigRepository.save(config);
        } catch (DataIntegrityViolationException e) {
            if(e.getMessage().contains("fk_crm_config_company")) {
                throw new RecordNotFoundException("Company not found with companyId=" + config.getCompanyId());
            }
            throw e;
        }
    }

    public List<CrmConfig> getCrmConfigsForCompany(Long companyId) {
        return crmConfigRepository.findByCompanyId(companyId);
    }
}
