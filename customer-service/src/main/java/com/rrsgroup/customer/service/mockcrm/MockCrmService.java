package com.rrsgroup.customer.service.mockcrm;

import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.customer.domain.*;
import com.rrsgroup.customer.entity.CrmConfig;
import com.rrsgroup.customer.service.CrmService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("mockCrmService")
public class MockCrmService implements CrmService {
    List<CrmCustomer> customers = List.of(
            CrmCustomer.builder().customerType(CrmCustomerType.RESIDENTIAL).crmCustomerId("3").firstName("John").lastName("Doe").address(CrmAddress.builder().address1("123 Main St.").city("Atlanta").state("Georgia").zipcode("30303").country("USA").build()).phoneNumber(CrmPhoneNumber.builder().phoneNumber(1231231234L).countryCode(1).build()).email("john.doe@test.com").build(),
            CrmCustomer.builder().customerType(CrmCustomerType.COMMERCIAL).crmCustomerId("4").companyName("L & L Flooring").firstName("Jane").lastName("Doe").address(CrmAddress.builder().address1("456 Main St.").city("Atlanta").state("Georgia").zipcode("30303").country("USA").build()).phoneNumber(CrmPhoneNumber.builder().phoneNumber(4564564567L).countryCode(1).build()).email("jane.doe@test.com").build()
    );

    @Override
    public CrmCustomer createCustomer(CrmCustomer crmCustomer, CrmConfig crmConfig, CompanyUserDto userDto) {
        throw new IllegalRequestException("Customers cannot be created for companies using the Mock CRM");
    }

    @Override
    public Optional<CrmCustomer> getCustomerById(String crmCustomerId, CrmConfig crmConfig) {
        return customers.stream().filter(customer -> customer.getCrmCustomerId().equals(crmCustomerId)).findFirst();
    }

    @Override
    public List<CrmCustomer> searchCustomers(CustomerSearchRequest request, CrmConfig crmConfig) {
        String companyNameSnippet = request.getCompanyName();
        String firstNameSnippet = request.getFirstName();
        String lastNameSnippet = request.getLastName();
        String addressSnippet = request.getAddress();
        String phoneNumberSnippet = request.getPhoneNumber();

        return customers.stream().filter(customer -> {
            if(StringUtils.isNotBlank(companyNameSnippet) && StringUtils.containsIgnoreCase(customer.getCompanyName(), companyNameSnippet)) {
                return true;
            }

            if(StringUtils.isNotBlank(firstNameSnippet) && StringUtils.containsIgnoreCase(customer.getFirstName(), firstNameSnippet)) {
                return true;
            }

            if(StringUtils.isNotBlank(lastNameSnippet) && StringUtils.containsIgnoreCase(customer.getLastName(), lastNameSnippet)) {
                return true;
            }

            if(StringUtils.isNotBlank(addressSnippet) && (
                    StringUtils.containsIgnoreCase(customer.getAddress().getAddress1(), addressSnippet) ||
                    StringUtils.containsIgnoreCase(customer.getAddress().getAddress2(), addressSnippet) ||
                    StringUtils.containsIgnoreCase(customer.getAddress().getCity(), addressSnippet) ||
                    StringUtils.containsIgnoreCase(customer.getAddress().getState(), addressSnippet) ||
                    StringUtils.containsIgnoreCase(customer.getAddress().getZipcode(), addressSnippet) ||
                    StringUtils.containsIgnoreCase(customer.getAddress().getCountry(), addressSnippet))) {
                return true;
            }

            if(phoneNumberSnippet != null && StringUtils.containsIgnoreCase(customer.getPhoneNumber().getPhoneNumber().toString(), phoneNumberSnippet)) {
                return true;
            }

            return false;
        }).toList();
    }
}
