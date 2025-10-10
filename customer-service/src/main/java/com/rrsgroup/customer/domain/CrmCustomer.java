package com.rrsgroup.customer.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrmCustomer {
    private String crmCustomerId;
    private String firstName;
    private String lastName;
    private CrmAddress address;
    private CrmPhoneNumber phoneNumber;
}
