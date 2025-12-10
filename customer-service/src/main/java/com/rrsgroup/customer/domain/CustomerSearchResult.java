package com.rrsgroup.customer.domain;

import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.QrCode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerSearchResult {
    private CrmCustomer crmCustomer;
    private Customer customer;
    private QrCode qrCode;
}
