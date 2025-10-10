package com.rrsgroup.customer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerSearchRequest {
    private String firstName;
    private String lastName;
    private String crmCustomerId;
    private String address;
    private Integer phoneNumber;
}
