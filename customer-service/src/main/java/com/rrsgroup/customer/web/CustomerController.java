package com.rrsgroup.customer.web;

import com.rrsgroup.common.dto.FieldUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.dto.CustomersSearchResultDto;
import com.rrsgroup.customer.entity.CrmConfig;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.service.CrmConfigService;
import com.rrsgroup.customer.service.CrmCustomerDtoMapper;
import com.rrsgroup.customer.service.CrmService;
import com.rrsgroup.customer.service.CustomerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class CustomerController {
    private final Map<String, CrmService> crmServices;
    private final CrmConfigService crmConfigService;
    private final CustomerService customerService;
    private final CrmCustomerDtoMapper crmCustomerDtoMapper;

    @Autowired
    public CustomerController(
            Map<String, CrmService> crmServices,
            CrmConfigService crmConfigService,
            CustomerService customerService,
            CrmCustomerDtoMapper crmCustomerDtoMapper) {
        this.crmServices = crmServices;
        this.crmConfigService = crmConfigService;
        this.customerService = customerService;
        this.crmCustomerDtoMapper = crmCustomerDtoMapper;
    }

    @PostMapping(value = "/api/field/customers/search", consumes = "application/x-www-form-urlencoded")
    public CustomersSearchResultDto customersSearch(
            @AuthenticationPrincipal FieldUserDto fieldUserDto,
            @RequestParam(name = "firstName", required = false) String firstNameSnippet,
            @RequestParam(name = "lastName", required = false) String lastNameSnippet,
            @RequestParam(name = "crmCustomerId", required = false) String crmCustomerId,
            @RequestParam(name = "address", required = false) String addressSnippet,
            @RequestParam(name = "phoneNumber", required = false) Integer phoneNumberSnippet) {
        if(StringUtils.isBlank(firstNameSnippet)
                && StringUtils.isBlank(lastNameSnippet)
                && StringUtils.isBlank(crmCustomerId)
                && StringUtils.isBlank(addressSnippet)
                && phoneNumberSnippet == null) {
            throw new IllegalRequestException("At least one search parameter must be passed");
        }

        Long companyId = fieldUserDto.getCompanyId();
        List<CrmConfig> companyCrmConfigs = crmConfigService.getCrmConfigsForCompany(companyId);
        List<Pair<CrmCustomer, Customer>> customerPairs = new ArrayList<>();

        for(CrmConfig crmConfig : companyCrmConfigs) {
            CrmService crmService = getCrmServiceForCrmConfig(crmConfig);

            if(StringUtils.isNotBlank(crmCustomerId)) {
                Optional<CrmCustomer> searchResult = crmService.getCustomerById(crmCustomerId);
                if(searchResult.isPresent()) {
                    CrmCustomer crmCustomer = searchResult.get();
                    Customer customer = customerService.getCustomerByCrmConfig(crmConfig, crmCustomer);

                    if(customer == null) {
                        customer = customerService.createCustomer(crmConfig, crmCustomer, fieldUserDto);
                    }

                    customerPairs.add(Pair.of(crmCustomer, customer));
                }
            } else {
                List<CrmCustomer> crmCustomers = crmService.searchCustomers(firstNameSnippet, lastNameSnippet, addressSnippet, phoneNumberSnippet);

                if(crmCustomers != null) {
                    List<Customer> customers = customerService.getCustomersByCrmConfig(crmConfig, crmCustomers);

                    // TODO: Convert this to parallel streams; customerPairs will have to be threadsafe
                    for(CrmCustomer crmCustomer : crmCustomers) {
                        Customer customer = customers.stream().filter(c -> c.getCrmCustomerId().equals(crmCustomer.getCrmCustomerId())).findFirst().orElse(null);

                        if(customer == null) {
                            customer = customerService.createCustomer(crmConfig, crmCustomer, fieldUserDto);
                        }

                        customerPairs.add(Pair.of(crmCustomer, customer));
                    }
                }
            }
        }

        return crmCustomerDtoMapper.map(customerPairs);
    }

    private CrmService getCrmServiceForCrmConfig(CrmConfig crmConfig) {
        CrmService crmService = crmServices.get(crmConfig.getCrmType().getCrmServiceName());

        if(crmService == null) {
            throw new IllegalStateException("CRMService is not configured for CrmType=" + crmConfig.getCrmType());
        }

        return crmService;
    }
}
