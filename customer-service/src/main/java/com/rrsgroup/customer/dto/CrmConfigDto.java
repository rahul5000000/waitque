package com.rrsgroup.customer.dto;

import com.rrsgroup.customer.domain.CrmType;

public record CrmConfigDto(Long id, Long companyId, CrmType crmType, String crmName) {
}
