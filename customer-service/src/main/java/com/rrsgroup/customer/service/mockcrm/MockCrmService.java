package com.rrsgroup.customer.service.mockcrm;

import com.rrsgroup.customer.domain.CrmAddress;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.CrmPhoneNumber;
import com.rrsgroup.customer.domain.CustomerSearchRequest;
import com.rrsgroup.customer.service.CrmService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("mockCrmService")
public class MockCrmService implements CrmService {
    List<CrmCustomer> customers = List.of(
            CrmCustomer.builder().crmCustomerId("1").firstName("John").lastName("Doe").address(CrmAddress.builder().address1("123 Main St.").city("Atlanta").state("Georgia").zipcode("30303").country("USA").build()).phoneNumber(CrmPhoneNumber.builder().phoneNumber(1231231234L).countryCode(1).build()).email("john.doe@test.com").build(),
            CrmCustomer.builder().crmCustomerId("2").firstName("Jane").lastName("Doe").address(CrmAddress.builder().address1("456 Main St.").city("Atlanta").state("Georgia").zipcode("30303").country("USA").build()).phoneNumber(CrmPhoneNumber.builder().phoneNumber(4564564567L).countryCode(1).build()).email("jane.doe@test.com").build()
    );

    @Override
    public Optional<CrmCustomer> getCustomerById(String crmCustomerId) {
        return customers.stream().filter(customer -> customer.getCrmCustomerId().equals(crmCustomerId)).findFirst();
    }

    @Override
    public List<CrmCustomer> searchCustomers(CustomerSearchRequest request) {

        String firstNameSnippet = request.getFirstName();
        String lastNameSnippet = request.getLastName();
        String addressSnippet = request.getAddress();
        Integer phoneNumberSnippet = request.getPhoneNumber();

        return customers.stream().filter(customer -> {
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

            if(phoneNumberSnippet != null && StringUtils.containsIgnoreCase(customer.getPhoneNumber().getPhoneNumber().toString(), phoneNumberSnippet.toString())) {
                return true;
            }

            return false;
        }).toList();
    }
}
