package com.rrsgroup.customer.service.nativecrm;

import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.common.entity.Address;
import com.rrsgroup.common.entity.Email;
import com.rrsgroup.common.entity.PhoneNumber;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.customer.domain.*;
import com.rrsgroup.customer.entity.CrmConfig;
import com.rrsgroup.customer.entity.nativecrm.NativeCrmConfig;
import com.rrsgroup.customer.entity.nativecrm.NativeCrmCustomer;
import com.rrsgroup.customer.repository.nativecrm.NativeCrmConfigRepository;
import com.rrsgroup.customer.repository.nativecrm.NativeCrmCustomerRepository;
import com.rrsgroup.customer.service.CrmService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service("waitqueCrmService")
public class NativeCrmService implements CrmService {
    private final NativeCrmConfigRepository nativeCrmConfigRepository;
    private final NativeCrmCustomerRepository nativeCrmCustomerRepository;

    @Autowired
    public NativeCrmService(
            NativeCrmCustomerRepository nativeCrmCustomerRepository,
            NativeCrmConfigRepository nativeCrmConfigRepository) {
        this.nativeCrmCustomerRepository = nativeCrmCustomerRepository;
        this.nativeCrmConfigRepository = nativeCrmConfigRepository;
    }

    private String trimSafe(String value) {
        if(value != null) return value.trim();
        return null;
    }

    public CrmCustomer createCustomer(final CrmCustomer crmCustomer, CrmConfig crmConfig, CompanyUserDto userDto) {
        LocalDateTime now = LocalDateTime.now();
        String createdBy = userDto.getUserId();

        Address address = new Address();
        address.setAddress1(trimSafe(crmCustomer.getAddress().getAddress1()));
        address.setAddress2(trimSafe(crmCustomer.getAddress().getAddress2()));
        address.setCity(trimSafe(crmCustomer.getAddress().getCity()));
        address.setState(trimSafe(crmCustomer.getAddress().getState()));
        address.setZipcode(trimSafe(crmCustomer.getAddress().getZipcode()));
        address.setCountry(trimSafe(crmCustomer.getAddress().getCountry()));

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setCountryCode(crmCustomer.getPhoneNumber().getCountryCode());
        phoneNumber.setPhoneNumber(crmCustomer.getPhoneNumber().getPhoneNumber());

        Email email = Email.builder()
                .firstName(trimSafe(crmCustomer.getFirstName()))
                .lastName(trimSafe(crmCustomer.getLastName()))
                .email(trimSafe(crmCustomer.getEmail()))
                .createdDate(now)
                .createdBy(createdBy)
                .updatedDate(now)
                .updatedBy(createdBy)
                .build();

        // TODO: prevent duplicate customers
        NativeCrmCustomer nativeCrmCustomer = NativeCrmCustomer.builder()
                .tenantId(getTenantId(crmConfig))
                .customerType(crmCustomer.getCustomerType())
                .companyName(trimSafe(crmCustomer.getCompanyName()))
                .firstName(trimSafe(crmCustomer.getFirstName()))
                .lastName(trimSafe(crmCustomer.getLastName()))
                .address(address)
                .phoneNumber(phoneNumber)
                .email(email)
                .createdDate(now)
                .createdBy(createdBy)
                .updatedDate(now)
                .updatedBy(createdBy)
                .build();

        NativeCrmCustomer savedNativeCrmCustomer = nativeCrmCustomerRepository.save(nativeCrmCustomer);

        return convertToDomain(savedNativeCrmCustomer);
    }

    private Long getTenantId(CrmConfig crmConfig) {
        Optional<NativeCrmConfig> nativeCrmConfigOptional = nativeCrmConfigRepository.findByCompanyId(crmConfig.getCompanyId());

        if(nativeCrmConfigOptional.isEmpty()) {
            throw new RuntimeException("Company is not configured with Native CRM configs");
        }

        return nativeCrmConfigOptional.get().getTenantId();
    }

    private Long convertCrmCustomerIdToId(String crmCustomerId) {
        try {
            return Long.parseLong(crmCustomerId);
        } catch (Exception e) {
            log.warn("Native CRM crmCustomerIds must be Long type");
            return -1L;
        }
    }

    @Override
    public Optional<CrmCustomer> getCustomerById(String crmCustomerId, CrmConfig crmConfig) {
        Long id = convertCrmCustomerIdToId(crmCustomerId);
        Long tenantId = getTenantId(crmConfig);

        Optional<NativeCrmCustomer> nativeCrmCustomerOptional = nativeCrmCustomerRepository.findByIdAndTenantId(id, tenantId);

        return nativeCrmCustomerOptional.map(this::convertToDomain);
    }

    @Override
    public List<CrmCustomer> searchCustomers(CustomerSearchRequest request, CrmConfig crmConfig) {
        Long tenantId = getTenantId(crmConfig);
        String companyNameSnippet = request.getCompanyName();
        String firstNameSnippet = request.getFirstName();
        String lastNameSnippet = request.getLastName();
        String addressSnippet = request.getAddress();
        String phoneNumberSnippet = request.getPhoneNumber();

        List<NativeCrmCustomer> searchResults = nativeCrmCustomerRepository.searchCustomers(
                tenantId,
                companyNameSnippet,
                firstNameSnippet,
                lastNameSnippet,
                addressSnippet,
                phoneNumberSnippet
        );

        return searchResults.stream().map(this::convertToDomain).toList();
    }

    private CrmCustomer convertToDomain(NativeCrmCustomer nativeCrmCustomer) {
        CrmAddress crmAddress = CrmAddress.builder()
                .address1(nativeCrmCustomer.getAddress().getAddress1())
                .address2(nativeCrmCustomer.getAddress().getAddress2())
                .city(nativeCrmCustomer.getAddress().getCity())
                .state(nativeCrmCustomer.getAddress().getState())
                .zipcode(nativeCrmCustomer.getAddress().getZipcode())
                .country(nativeCrmCustomer.getAddress().getCountry())
                .build();

        CrmPhoneNumber crmPhoneNumber = CrmPhoneNumber.builder()
                .countryCode(nativeCrmCustomer.getPhoneNumber().getCountryCode())
                .phoneNumber(nativeCrmCustomer.getPhoneNumber().getPhoneNumber())
                .build();

        return CrmCustomer.builder()
                .customerType(nativeCrmCustomer.getCustomerType())
                .crmCustomerId(nativeCrmCustomer.getId().toString())
                .companyName(nativeCrmCustomer.getCompanyName())
                .firstName(nativeCrmCustomer.getFirstName())
                .lastName(nativeCrmCustomer.getLastName())
                .address(crmAddress)
                .phoneNumber(crmPhoneNumber)
                .email(nativeCrmCustomer.getEmail().getEmail())
                .build();
    }

    public NativeCrmConfig initNativeCrm(CrmConfig crmConfig) {
        if(crmConfig.getCrmType() != CrmType.WAITQUE) {
            throw new IllegalRequestException("Cannot init Native CRM for this crmConfig: " + crmConfig.getCrmType());
        }

        NativeCrmConfig nativeCrmConfig = NativeCrmConfig.builder()
                .companyId(crmConfig.getCompanyId())
                .tenantId(crmConfig.getCompanyId())
                .build();

        return nativeCrmConfigRepository.save(nativeCrmConfig);
    }
}
