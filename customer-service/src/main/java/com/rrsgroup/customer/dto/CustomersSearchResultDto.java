package com.rrsgroup.customer.dto;

import com.rrsgroup.customer.domain.CrmCustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CustomersSearchResultDto {
    List<CustomersSearchResultItem> customers;

    public CustomersSearchResultDto(List<CustomersSearchResultDto.CustomersSearchResultItem> customers) {
        this.customers = customers;
    }

    @Data
    @AllArgsConstructor
    public static class CustomersSearchResultItem {
        private Long id;
        private CrmCustomerType customerType;
        private String companyName;
        private String firstName;
        private String lastName;
        private String crmCustomerId;
        private Boolean isAssociated;
    }
}
