package com.rrsgroup.customer.service;

import com.rrsgroup.customer.domain.CrmCustomer;

import java.util.List;
import java.util.Optional;

public interface CrmService {
    Optional<CrmCustomer> getCustomerById(String crmCustomerId);
    List<CrmCustomer> searchCustomers(String firstNameSnippet, String lastNameSnippet, String addressSnippet, Integer phoneNumberSnippet);
}
