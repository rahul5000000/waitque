package com.rrsgroup.customer.domain;

import com.rrsgroup.customer.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CrmCustomerCreateResult {
    private CrmCustomer crmCustomer;
    private Customer customer;
}
