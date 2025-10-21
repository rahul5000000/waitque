package com.rrsgroup.customer.service;

import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.entity.CrmConfig;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.QrCode;
import com.rrsgroup.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final QrCodeService qrCodeService;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, QrCodeService qrCodeService) {
        this.customerRepository = customerRepository;
        this.qrCodeService = qrCodeService;
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

    public Optional<Customer> getCustomerByQrCode(UUID qrCode) {
        Optional<QrCode> qrCodeOptional = qrCodeService.getAssociatedQrCode(qrCode);

        if(qrCodeOptional.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(qrCodeOptional.get().getCustomer());
    }

    public Customer getCustomerByQrCodeSafe(UUID qrCode) {
        Optional<Customer> customerOptional = getCustomerByQrCode(qrCode);

        if(customerOptional.isEmpty()) {
            throw new RecordNotFoundException("Customer does not exist with qrCode=" + qrCode);
        }

        return customerOptional.get();
    }
}
