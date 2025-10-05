package com.rrsgroup.customer.service;

import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.dto.CustomersSearchResultDto;
import com.rrsgroup.customer.entity.Customer;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrmCustomerDtoMapper {
    public CustomersSearchResultDto map(List<Pair<CrmCustomer, Customer>> customers) {
        return new CustomersSearchResultDto(customers.stream().map(customerPair -> {
            Customer customer = customerPair.getRight();
            CrmCustomer crmCustomer = customerPair.getLeft();
            return new CustomersSearchResultDto.CustomersSearchResultItem(
                    customer.getId(),
                    crmCustomer.getFirstName(),
                    crmCustomer.getLastName(),
                    crmCustomer.getCrmCustomerId(),
                    false);
        }).toList());
    }
}
