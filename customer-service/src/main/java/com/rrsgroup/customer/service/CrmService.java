package com.rrsgroup.customer.service;

import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.CustomerSearchRequest;

import java.util.List;
import java.util.Optional;

public interface CrmService {
    Optional<CrmCustomer> getCustomerById(String crmCustomerId);
    List<CrmCustomer> searchCustomers(CustomerSearchRequest request);
}
