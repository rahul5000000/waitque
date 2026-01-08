package com.rrsgroup.customer.service;

import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.customer.domain.*;
import com.rrsgroup.customer.dto.CustomerDetailDto;
import com.rrsgroup.customer.dto.CustomersSearchResultDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.CustomerCode;
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

    public CrmAddress map(AddressDto dto) {
        return CrmAddress.builder()
                .address1(dto.address1())
                .address2(dto.address2())
                .city(dto.city())
                .state(dto.state())
                .zipcode(dto.zipcode())
                .country(dto.country())
                .build();
    }

    public AddressDto map(CrmAddress address) {
        return new AddressDto(null, address.getAddress1(), address.getAddress2(), address.getCity(),
                address.getState(), address.getZipcode(), address.getCountry());
    }

    public CrmPhoneNumber map(PhoneNumberDto dto) {
        return CrmPhoneNumber.builder()
                .countryCode(dto.countryCode())
                .phoneNumber(dto.phoneNumber())
                .build();
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

        String customerCode = null;
        if(searchResult.getCustomer().getCustomerCodes() != null) {
            Optional<CustomerCode> activeCustomerCode = searchResult.getCustomer().getCustomerCodes().stream().filter(assignedCustomerCode -> assignedCustomerCode.getStatus() == CustomerCode.CustomerCodeStatus.ACTIVE).findFirst();

            if(activeCustomerCode.isPresent()) {
                customerCode = activeCustomerCode.get().getCustomerCode();
            }
        }

        return new CustomerDetailDto(customer.getId(), crmCustomer.getCustomerType(), crmCustomer.getCompanyName(),
                crmCustomer.getFirstName(), crmCustomer.getLastName(),
                map(crmCustomer.getAddress()), map(crmCustomer.getPhoneNumber()),
                crmCustomer.getEmail(), frontEndLink, customerCode);
    }

    public CrmCustomer map(CustomerDetailDto dto) {
        return CrmCustomer.builder()
                .customerType(dto.customerType())
                .companyName(dto.companyName())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .address(map(dto.address()))
                .phoneNumber(map(dto.phone()))
                .email(dto.email())
                .build();
    }

    public CustomerDetailDto map(CrmCustomerCreateResult result) {
        Customer customer = result.getCustomer();
        CrmCustomer crmCustomer = result.getCrmCustomer();

        String customerCode = null;
        if(result.getCustomer().getCustomerCodes() != null) {
            Optional<CustomerCode> activeCustomerCode = result.getCustomer().getCustomerCodes().stream().filter(assignedCustomerCode -> assignedCustomerCode.getStatus() == CustomerCode.CustomerCodeStatus.ACTIVE).findFirst();

            if(activeCustomerCode.isPresent()) {
                customerCode = activeCustomerCode.get().getCustomerCode();
            }
        }

        return new CustomerDetailDto(customer.getId(), crmCustomer.getCustomerType(), crmCustomer.getCompanyName(),
                crmCustomer.getFirstName(), crmCustomer.getLastName(),
                map(crmCustomer.getAddress()), map(crmCustomer.getPhoneNumber()),
                crmCustomer.getEmail(), null, customerCode);
    }
}
