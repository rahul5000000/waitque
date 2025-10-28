package com.rrsgroup.customer.service;

import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.dto.CustomerDto;
import com.rrsgroup.customer.dto.PublicCustomerDetailDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.QrCode;
import org.springframework.stereotype.Service;

@Service
public class CustomerDtoMapper {
    public CustomerDto map(QrCode qrCode) {
        Long customerId = null;
        String crmCustomerId = null;
        Customer customer = qrCode.getCustomer();

        if(customer != null) {
            customerId = customer.getId();
            crmCustomerId = customer.getCrmCustomerId();
        }

        return new CustomerDto(customerId, crmCustomerId, qrCode.getQrCode());
    }

    public CustomerDto map(Customer customer) {
        return new CustomerDto(customer.getId(), customer.getCrmCustomerId(), null);
    }

    public PublicCustomerDetailDto map(Customer customer, CrmCustomer crmCustomer) {
        return new PublicCustomerDetailDto(customer.getId(), crmCustomer.getCompanyName(), crmCustomer.getFirstName(), crmCustomer.getLastName());
    }
}
