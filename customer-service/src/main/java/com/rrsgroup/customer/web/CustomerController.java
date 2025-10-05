package com.rrsgroup.customer.web;

import com.rrsgroup.common.dto.FieldUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.CustomerSearchRequest;
import com.rrsgroup.customer.domain.CustomerSearchResult;
import com.rrsgroup.customer.dto.CustomersSearchResultDto;
import com.rrsgroup.customer.entity.CrmConfig;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class CustomerController {
    private final CrmCustomerDtoMapper crmCustomerDtoMapper;
    private final CustomerCrmIntegrationService integrationService;

    @Autowired
    public CustomerController(CrmCustomerDtoMapper crmCustomerDtoMapper, CustomerCrmIntegrationService integrationService) {
        this.crmCustomerDtoMapper = crmCustomerDtoMapper;
        this.integrationService = integrationService;
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
}
