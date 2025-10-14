package com.rrsgroup.customer.service;

import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.entity.CrmConfig;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(CrmConfig crmConfig, CrmCustomer crmCustomer, CompanyUserDto createdBy) {
        LocalDateTime now = LocalDateTime.now();
        String userId = createdBy.getUserId();

        Customer customer = Customer.builder()
                .crmCustomerId(crmCustomer.getCrmCustomerId())
                .crmConfig(crmConfig)
                .createdBy(userId)
                .createdDate(now)
                .updatedBy(userId)
                .updatedDate(now)
                .build();

        return customerRepository.save(customer);
    }

    public Customer getCustomerByCrmConfig(CrmConfig crmConfig, CrmCustomer crmCustomer) {
        return customerRepository.findByCrmCustomerIdAndCrmConfig_Id(crmCustomer.getCrmCustomerId(), crmConfig.getId());
    }

    public List<Customer> getCustomersByCrmConfig(CrmConfig crmConfig, List<CrmCustomer> crmCustomers) {
        List<String> crmCustomerIds = crmCustomers.stream().map(CrmCustomer::getCrmCustomerId).toList();
        return customerRepository.findAllByCrmCustomerIdInAndCrmConfig_Id(crmCustomerIds, crmConfig.getId());
    }

    public Optional<Customer> getCustomerById(Long customerId, CompanyUserDto userDto) {
        return customerRepository.findByIdAndCrmConfig_CompanyId(customerId, userDto.getCompanyId());
    }

    public Optional<Customer> getCustomerById(Long customerId, Long companyId) {
        return customerRepository.findByIdAndCrmConfig_CompanyId(customerId, companyId);
    }
}
