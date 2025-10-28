package com.rrsgroup.customer.dto;

import com.rrsgroup.customer.domain.CrmCustomerType;

public record PublicCustomerDetailDto(Long id, CrmCustomerType customerType, String companyName, String firstName, String lastName) {
}
