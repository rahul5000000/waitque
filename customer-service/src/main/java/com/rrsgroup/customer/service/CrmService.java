package com.rrsgroup.customer.service;

import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.CustomerSearchRequest;
import com.rrsgroup.customer.entity.CrmConfig;

import java.util.List;
import java.util.Optional;

public interface CrmService {
    CrmCustomer createCustomer(CrmCustomer crmCustomer, CrmConfig crmConfig, CompanyUserDto userDto);
    Optional<CrmCustomer> getCustomerById(String crmCustomerId, CrmConfig crmConfig);
    List<CrmCustomer> searchCustomers(CustomerSearchRequest request, CrmConfig crmConfig);
}
