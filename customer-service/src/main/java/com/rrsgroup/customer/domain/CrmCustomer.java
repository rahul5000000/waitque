package com.rrsgroup.customer.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CrmCustomer {
    private CrmCustomerType customerType;
    private String crmCustomerId;
    private String companyName;
    private String firstName;
    private String lastName;
    private CrmAddress address;
    private CrmPhoneNumber phoneNumber;
    private List<CrmPhoneNumber> additionalPhoneNumbers;
    private String email;
}
