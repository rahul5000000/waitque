package com.rrsgroup.customer.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrmPhoneNumber {
    private Integer countryCode;
    private Long phoneNumber;
}
