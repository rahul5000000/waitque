package com.rrsgroup.customer.service;

import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.customer.domain.CrmAddress;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.CrmPhoneNumber;
import com.rrsgroup.customer.domain.CustomerSearchResult;
import com.rrsgroup.customer.dto.CustomerDetailDto;
import com.rrsgroup.customer.dto.CustomersSearchResultDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.QrCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CrmCustomerDtoMapper {
    private final CompanyService companyService;

    @Autowired
    public CrmCustomerDtoMapper(CompanyService companyService) {
        this.companyService = companyService;
    }

    public CustomersSearchResultDto map(List<CustomerSearchResult> searchResults) {
        return new CustomersSearchResultDto(searchResults.stream().map(searchRecord -> {
            Customer customer = searchRecord.getCustomer();
            CrmCustomer crmCustomer = searchRecord.getCrmCustomer();
            return new CustomersSearchResultDto.CustomersSearchResultItem(
                    customer.getId(),
                    crmCustomer.getCustomerType(),
                    crmCustomer.getCompanyName(),
                    crmCustomer.getFirstName(),
                    crmCustomer.getLastName(),
                    crmCustomer.getCrmCustomerId(),
                    searchRecord.getQrCode() != null);
        }).toList());
    }

    public AddressDto map(CrmAddress address) {
        return new AddressDto(null, address.getAddress1(), address.getAddress2(), address.getCity(),
                address.getState(), address.getZipcode(), address.getCountry());
    }

    public PhoneNumberDto map(CrmPhoneNumber phoneNumber) {
        return new PhoneNumberDto(null, phoneNumber.getCountryCode(), phoneNumber.getPhoneNumber());
    }

    public CustomerDetailDto map(CustomerSearchResult searchResult) {
        Customer customer = searchResult.getCustomer();
        CrmCustomer crmCustomer = searchResult.getCrmCustomer();

        String frontEndLink = null;
        if(searchResult.getQrCode() != null) {
            Optional<String> frontEndLinkOptional = companyService.getQrCodeFrontEndLink(customer.getCrmConfig().getCompanyId(), searchResult.getQrCode().getQrCode());
            if(frontEndLinkOptional.isPresent()) {
                frontEndLink = frontEndLinkOptional.get();
            }
        }

        return new CustomerDetailDto(customer.getId(), crmCustomer.getCustomerType(), crmCustomer.getCompanyName(),
                crmCustomer.getFirstName(), crmCustomer.getLastName(),
                map(crmCustomer.getAddress()), map(crmCustomer.getPhoneNumber()),
                crmCustomer.getEmail(), frontEndLink);
    }
}
