package com.rrsgroup.customer.service;

import com.rrsgroup.common.dto.FieldUserDto;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.CustomerSearchRequest;
import com.rrsgroup.customer.domain.CustomerSearchResult;
import com.rrsgroup.customer.entity.CrmConfig;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.QrCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerCrmIntegrationService {
    private final Map<String, CrmService> crmServices;
    private final CrmConfigService crmConfigService;
    private final CustomerService customerService;
    private final QrCodeService qrCodeService;

    @Autowired
    public CustomerCrmIntegrationService(
            Map<String, CrmService> crmServices,
            CrmConfigService crmConfigService,
            CustomerService customerService,
            QrCodeService qrCodeService
    ) {
        this.crmServices = crmServices;
        this.crmConfigService = crmConfigService;
        this.customerService = customerService;
        this.qrCodeService = qrCodeService;
    }

    public List<CustomerSearchResult> customerSearch(FieldUserDto fieldUserDto,
                                                                       CustomerSearchRequest request) {
        List<CustomerSearchResult> searchResults = new ArrayList<>();

        Long companyId = fieldUserDto.getCompanyId();
        List<CrmConfig> companyCrmConfigs = crmConfigService.getCrmConfigsForCompany(companyId);

        for(CrmConfig crmConfig : companyCrmConfigs) {
            searchResults.addAll(searchCustomersInCrm(crmConfig, request, fieldUserDto));
        }

        return searchResults;
    }

    private List<CustomerSearchResult> searchCustomersInCrm(CrmConfig crmConfig, CustomerSearchRequest request, FieldUserDto fieldUserDto) {
        List<CustomerSearchResult> searchResults = new ArrayList<>();

        List<CrmCustomer> crmCustomers = searchCrmCustomers(crmConfig, request);
        List<Customer> customers = getCustomersMatchingCrmCustomers(crmConfig, crmCustomers);
        List<QrCode> qrCodes = qrCodeService.getQrCodesForCustomers(customers);

        // TODO: Convert this to parallel streams; customerPairs will have to be threadsafe
        // Map CrmCustomer and Customers
        for(CrmCustomer crmCustomer : crmCustomers) {
            Customer customer = customers.stream().filter(c -> c.getCrmCustomerId().equals(crmCustomer.getCrmCustomerId())).findFirst().orElse(null);
            Optional<QrCode> qrCodeOptional = Optional.empty();

            if(customer == null) {
                customer = customerService.createCustomer(crmConfig, crmCustomer, fieldUserDto);
            } else {
                // Optimization; only search for a QR code for customer that was not just created
                Customer finalCustomer = customer;
                qrCodeOptional = qrCodes.stream().filter(qrCode -> qrCode.getCustomer().getId().equals(finalCustomer.getId())).findFirst();
            }

            searchResults.add(new CustomerSearchResult(crmCustomer, customer, qrCodeOptional.isPresent()));
        }

        return searchResults;
    }

    private List<CrmCustomer> searchCrmCustomers(CrmConfig crmConfig, CustomerSearchRequest request) {
        CrmService crmService = getCrmServiceForCrmConfig(crmConfig);

        if(StringUtils.isNotBlank(request.getCrmCustomerId())) {
            return crmService.getCustomerById(request.getCrmCustomerId()).map(List::of).orElseGet(List::of);
        } else {
            return crmService.searchCustomers(request);
        }
    }

    private List<Customer> getCustomersMatchingCrmCustomers(CrmConfig crmConfig, List<CrmCustomer> crmCustomers) {
        if(crmCustomers.size() == 1) {
            Customer customer = customerService.getCustomerByCrmConfig(crmConfig, crmCustomers.get(0));
            if(customer != null) return List.of(customer);
            else return List.of();
        } else {
            return customerService.getCustomersByCrmConfig(crmConfig, crmCustomers);
        }
    }

    private CrmService getCrmServiceForCrmConfig(CrmConfig crmConfig) {
        CrmService crmService = crmServices.get(crmConfig.getCrmType().getCrmServiceName());

        if(crmService == null) {
            throw new IllegalStateException("CRMService is not configured for CrmType=" + crmConfig.getCrmType());
        }

        return crmService;
    }
}
