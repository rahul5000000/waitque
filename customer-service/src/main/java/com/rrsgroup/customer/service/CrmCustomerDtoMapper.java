package com.rrsgroup.customer.service;

import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.CustomerSearchResult;
import com.rrsgroup.customer.dto.CustomersSearchResultDto;
import com.rrsgroup.customer.entity.Customer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrmCustomerDtoMapper {
    public CustomersSearchResultDto map(List<CustomerSearchResult> searchResults) {
        return new CustomersSearchResultDto(searchResults.stream().map(searchRecord -> {
            Customer customer = searchRecord.getCustomer();
            CrmCustomer crmCustomer = searchRecord.getCrmCustomer();
            return new CustomersSearchResultDto.CustomersSearchResultItem(
                    customer.getId(),
                    crmCustomer.getFirstName(),
                    crmCustomer.getLastName(),
                    crmCustomer.getCrmCustomerId(),
                    searchRecord.getQrCodeIsAssociated());
        }).toList());
    }
}
