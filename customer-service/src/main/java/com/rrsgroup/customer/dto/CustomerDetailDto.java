package com.rrsgroup.customer.dto;

import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.customer.domain.CrmCustomerType;

import java.util.UUID;

public record CustomerDetailDto(Long id, CrmCustomerType customerType, String companyName, String firstName,
                                String lastName, AddressDto address, PhoneNumberDto phone, String email,
                                String frontEndLink, String customerCode) {
}
