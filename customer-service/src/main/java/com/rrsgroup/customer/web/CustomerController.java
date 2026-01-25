package com.rrsgroup.customer.web;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.common.dto.FieldUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.CrmCustomerCreateResult;
import com.rrsgroup.customer.domain.CustomerSearchRequest;
import com.rrsgroup.customer.domain.CustomerSearchResult;
import com.rrsgroup.customer.dto.*;
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
import java.util.UUID;

@Log4j2
@RestController
public class CustomerController {
    private final CrmCustomerDtoMapper crmCustomerDtoMapper;
    private final CustomerCrmIntegrationService integrationService;
    private final CustomerService customerService;
    private final QrCodeService qrCodeService;
    private final CustomerDtoMapper customerDtoMapper;
    private final CompanyService companyService;
    private final LeadFlowService leadFlowService;
    private final EventService eventService;

    @Autowired
    public CustomerController(
            CrmCustomerDtoMapper crmCustomerDtoMapper,
            CustomerCrmIntegrationService integrationService,
            CustomerService customerService,
            QrCodeService qrCodeService,
            CustomerDtoMapper customerDtoMapper,
            CompanyService companyService,
            LeadFlowService leadFlowService,
            EventService eventService) {
        this.crmCustomerDtoMapper = crmCustomerDtoMapper;
        this.integrationService = integrationService;
        this.customerService = customerService;
        this.qrCodeService = qrCodeService;
        this.customerDtoMapper = customerDtoMapper;
        this.companyService = companyService;
        this.leadFlowService = leadFlowService;
        this.eventService = eventService;
    }

    @PostMapping("/api/field/customers")
    public CustomerDetailDto createCrmCustomer(
            @AuthenticationPrincipal FieldUserDto fieldUserDto,
            @RequestBody CustomerDetailDto request) {
        CrmCustomer crmCustomer = crmCustomerDtoMapper.map(request);
        CrmCustomerCreateResult result = integrationService.createCrmCustomer(crmCustomer, fieldUserDto);

        return crmCustomerDtoMapper.map(result);
    }

    @PostMapping(value = "/api/field/customers/search", consumes = "application/x-www-form-urlencoded")
    public CustomersSearchResultDto customersSearch(
            @AuthenticationPrincipal FieldUserDto fieldUserDto,
            @ModelAttribute CustomerSearchRequest request) {
        if(StringUtils.isBlank(request.getCompanyName())
                && StringUtils.isBlank(request.getFirstName())
                && StringUtils.isBlank(request.getLastName())
                && StringUtils.isBlank(request.getCrmCustomerId())
                && StringUtils.isBlank(request.getAddress())
                && request.getAddress() == null
                && StringUtils.isBlank(request.getPhoneNumber())
                && request.getPhoneNumber() == null) {
            throw new IllegalRequestException("At least one search parameter must be passed");
        }

        List<CustomerSearchResult> searchResults = integrationService.customerSearch(fieldUserDto, request);
        return crmCustomerDtoMapper.map(searchResults);
    }

    @GetMapping("/api/field/customers/{customerId}")
    public CustomerDetailDto getCustomer(
            @AuthenticationPrincipal FieldUserDto fieldUserDto,
            @PathVariable("customerId") Long customerId) {
        CustomerSearchResult customerSearchResult = integrationService.getCustomer(customerId, fieldUserDto);
        return crmCustomerDtoMapper.map(customerSearchResult);
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

    @GetMapping("/api/public/customers/qrCode/{qrCode}/company")
    public CompanyDto getCompanyForCustomer(@PathVariable("qrCode") UUID qrCode) {
        Customer customer = customerService.getCustomerByQrCodeSafe(qrCode);
        Optional<CompanyDto> companyOptional = companyService.getCompany(customer.getCrmConfig().getCompanyId());

        if(companyOptional.isEmpty()) {
            throw new RecordNotFoundException("Company not found for customer with qrCode=" + qrCode);
        }

        return companyOptional.get();
    }

    @GetMapping("/api/public/customers/qrCode/{qrCode}/company/flows")
    public ActiveLeadFlowListDto publicGetListOfLeadFlows(
            @PathVariable(name = "qrCode") UUID qrCode,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "sortField", required = false, defaultValue = "ordinal") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") SortDirection sortDir) {
        Customer customer = customerService.getCustomerByQrCodeSafe(qrCode);
        Optional<ActiveLeadFlowListDto> leadFlowListOptional = leadFlowService.getLeadFlows(
                customer.getCrmConfig().getCompanyId(),
                limit,
                page,
                sortField,
                sortDir);

        if(leadFlowListOptional.isEmpty()) {
            throw new RecordNotFoundException("Lead flows not found for company associated with qrCode=" + qrCode);
        }

        return leadFlowListOptional.get();
    }

    @GetMapping("/api/public/customers/qrCode/{qrCode}/company/leadFlows/{leadFlowId}")
    public LeadFlowDto publicGetLeadFlow(
            @PathVariable(name = "qrCode") UUID qrCode,
            @PathVariable(name = "leadFlowId") Long leadFlowId) {
        Customer customer = customerService.getCustomerByQrCodeSafe(qrCode);
        Optional<LeadFlowDto> leadFlowOptional = leadFlowService.getLeadFlow(leadFlowId, customer.getCrmConfig().getCompanyId());

        if(leadFlowOptional.isEmpty()) {
            throw new RecordNotFoundException("Lead flow not found with leadFlowId=" + leadFlowId + ", qrCode=" + qrCode);
        }

        return leadFlowOptional.get();
    }

    @GetMapping("/api/public/customers/qrCode/{qrCode}/me")
    public PublicCustomerDetailDto publicGetCustomer(@PathVariable(name = "qrCode") UUID qrCode) {
        Customer customer = customerService.getCustomerByQrCodeSafe(qrCode);
        Optional<CrmCustomer> crmCustomerOptional = integrationService.getCrmCustomer(customer.getCrmCustomerId(), customer.getCrmConfig());

        if(crmCustomerOptional.isEmpty()) {
            throw new RecordNotFoundException("Customer information not found for qrCode=" + qrCode);
        }

        eventService.customerLogin(customer, qrCode);

        return customerDtoMapper.map(customer, crmCustomerOptional.get());
    }

    @PostMapping("/api/public/customers/qrCode/{qrCode}/mobileLogs")
    public void createMobileLogs(@PathVariable(name = "qrCode") UUID qrCode, @RequestBody MobileLogDto request) {
        Customer customer = customerService.getCustomerByQrCodeSafe(qrCode);
        log.log(request.level().getLog4jLevel(), "Mobile Logs: customer={}, platform={}, page={}, message={}, json={}", customer.getId(), request.platform(), request.page(), request.message(), request.json());
    }

    @PostMapping({"/api/admin/mobileLogs", "/api/field/mobileLogs"})
    public void createMobileLogs(@AuthenticationPrincipal CompanyUserDto user, @RequestBody MobileLogDto request) {
        log.log(request.level().getLog4jLevel(), "Mobile Logs: user={}, company={}, platform={}, page={}, message={}, json={}", user.getUserId(), user.getCompanyId(), request.platform(), request.page(), request.message(), request.json());
    }
}
