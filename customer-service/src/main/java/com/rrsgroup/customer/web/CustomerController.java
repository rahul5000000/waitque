package com.rrsgroup.customer.web;

import com.rrsgroup.common.dto.FieldUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.customer.domain.CustomerSearchRequest;
import com.rrsgroup.customer.domain.CustomerSearchResult;
import com.rrsgroup.customer.dto.AssociateQrCodeDto;
import com.rrsgroup.customer.dto.CustomerDto;
import com.rrsgroup.customer.dto.CustomersSearchResultDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.QrCode;
import com.rrsgroup.customer.service.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Log4j2
@RestController
public class CustomerController {
    private final CrmCustomerDtoMapper crmCustomerDtoMapper;
    private final CustomerCrmIntegrationService integrationService;
    private final CustomerService customerService;
    private final QrCodeService qrCodeService;
    private final CustomerDtoMapper customerDtoMapper;

    @Autowired
    public CustomerController(
            CrmCustomerDtoMapper crmCustomerDtoMapper,
            CustomerCrmIntegrationService integrationService,
            CustomerService customerService,
            QrCodeService qrCodeService,
            CustomerDtoMapper customerDtoMapper) {
        this.crmCustomerDtoMapper = crmCustomerDtoMapper;
        this.integrationService = integrationService;
        this.customerService = customerService;
        this.qrCodeService = qrCodeService;
        this.customerDtoMapper = customerDtoMapper;
    }

    @PostMapping(value = "/api/field/customers/search", consumes = "application/x-www-form-urlencoded")
    public CustomersSearchResultDto customersSearch(
            @AuthenticationPrincipal FieldUserDto fieldUserDto,
            @ModelAttribute CustomerSearchRequest request) {
        if(StringUtils.isBlank(request.getFirstName())
                && StringUtils.isBlank(request.getLastName())
                && StringUtils.isBlank(request.getCrmCustomerId())
                && StringUtils.isBlank(request.getAddress())
                && request.getAddress() == null) {
            throw new IllegalRequestException("At least one search parameter must be passed");
        }

        List<CustomerSearchResult> searchResults = integrationService.customerSearch(fieldUserDto, request);
        return crmCustomerDtoMapper.map(searchResults);
    }

    @PutMapping("/api/field/customers/{customerId}/qrCode")
    public CustomerDto associateQrCodeWithCustomer(
            @AuthenticationPrincipal FieldUserDto fieldUserDto,
            @PathVariable("customerId") Long customerId,
            @RequestBody AssociateQrCodeDto request
            ) {
        // Does customer exist in user org?
        Optional<Customer> customerOptional = customerService.getCustomerById(customerId, fieldUserDto);
        if(customerOptional.isEmpty()) throw new RecordNotFoundException("Customer not found with customerId=" + customerId);
        Customer customer = customerOptional.get();

        // Does QR code exist in user org?
        Optional<QrCode> qrCodeOptional = qrCodeService.getQrCode(request.qrCode(), fieldUserDto);
        if(qrCodeOptional.isEmpty()) throw new RecordNotFoundException("QR code not found: " + request.qrCode());
        QrCode qrCode = qrCodeOptional.get();

        // Is customer unassociated?
        Optional<QrCode> qrCodeForCustomerOptional = qrCodeService.getQrCodeForCustomer(customer);
        if(qrCodeForCustomerOptional.isPresent()) {
            QrCode qrCodeForCustomer = qrCodeForCustomerOptional.get();

            if(qrCodeForCustomer.getQrCode().equals(request.qrCode())) {
                // This is a duplicate request; customer is already associated to QR code in the request
                log.warn("Duplicate associate request for qrCodeId={}. customerId={}", qrCodeForCustomer.getId(), customer.getId());
                return customerDtoMapper.map(qrCodeForCustomer);
            } else {
                log.error("Attempt to associate customerId={} to another qrCodeId={}; currently associated to qrCodeId={}", customerId, qrCode.getId(), qrCodeForCustomer.getId());
                throw new IllegalUpdateException("The customer is already associated with another QR code");
            }
        }

        // Is QR code unassociated?
        if(qrCode.getCustomer() != null) {
            log.error("Attempt to reuse QR code for customerId={}; qrCodeId={} is associated to customerId={}", customerId, qrCode.getId(), qrCode.getCustomer().getId());
            throw new IllegalUpdateException("The QR code is already associated with another customer");
        }

        return customerDtoMapper.map(qrCodeService.associateQrCodeToCustomer(qrCode, customer));
    }

    @DeleteMapping("/api/field/customers/{customerId}/qrCode")
    public CustomerDto disassociateQrCodeFromCustomer(
            @AuthenticationPrincipal FieldUserDto fieldUserDto,
            @PathVariable("customerId") Long customerId) {
        // Does customer exist in user org?
        Optional<Customer> customerOptional = customerService.getCustomerById(customerId, fieldUserDto);
        if(customerOptional.isEmpty()) throw new RecordNotFoundException("Customer not found with customerId=" + customerId);
        Customer customer = customerOptional.get();

        // Is customer unassociated?
        Optional<QrCode> qrCodeForCustomerOptional = qrCodeService.getQrCodeForCustomer(customer);
        if(qrCodeForCustomerOptional.isEmpty()) {
            log.warn("Attempted to disassociate QR code from customerId={} that does not have QR code associated", customerId);
            return customerDtoMapper.map(customer);
        }

        qrCodeService.disassociateQrCode(qrCodeForCustomerOptional.get());

        return customerDtoMapper.map(customer);
    }
}
