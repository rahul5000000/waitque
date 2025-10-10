package com.rrsgroup.customer.service;

import com.rrsgroup.customer.dto.CrmConfigDto;
import com.rrsgroup.customer.entity.CrmConfig;
import org.springframework.stereotype.Service;

@Service
public class CrmConfigDtoMapper {
    public CrmConfig map(CrmConfigDto dto) {
        return CrmConfig.builder().id(dto.id()).crmType(dto.crmType()).crmName(dto.crmName()).companyId(dto.companyId()).build();
    }

    public CrmConfigDto map(CrmConfig crmConfig) {
        return new CrmConfigDto(crmConfig.getId(), crmConfig.getCompanyId(), crmConfig.getCrmType(), crmConfig.getCrmName());
    }
}
