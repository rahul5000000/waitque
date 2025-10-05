package com.rrsgroup.customer.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrmAddress {
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zipcode;
    private String country;
}
