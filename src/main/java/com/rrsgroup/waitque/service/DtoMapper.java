package com.rrsgroup.waitque.service;

import com.rrsgroup.waitque.dto.AddressDto;
import com.rrsgroup.waitque.dto.CompanyDto;
import com.rrsgroup.waitque.dto.PhoneNumberDto;
import com.rrsgroup.waitque.entity.Address;
import com.rrsgroup.waitque.entity.Company;
import com.rrsgroup.waitque.entity.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class DtoMapper {
    public AddressDto map(Address address) {
        return new AddressDto(
                address.getId(),
                address.getAddress1(),
                address.getAddress2(),
                address.getCity(),
                address.getState(),
                address.getZipcode(),
                address.getCountry());
    }

    public Address map(AddressDto dto) {
        return new Address(
                dto.id(),
                dto.address1(),
                dto.address2(),
                dto.city(),
                dto.state(),
                dto.zipcode(),
                dto.country());
    }

    public PhoneNumberDto map(PhoneNumber phoneNumber) {
        return new PhoneNumberDto(phoneNumber.getId(), phoneNumber.getCountryCode(), phoneNumber.getPhoneNumber());
    }
    
    public PhoneNumber map(PhoneNumberDto dto) {
        return new PhoneNumber(dto.id(), dto.countryCode(), dto.phoneNumber());
    }

    public CompanyDto map(Company company, Address address, PhoneNumber phoneNumber) {
        AddressDto addressDto = map(address);
        PhoneNumberDto phoneNumberDto = map(phoneNumber);

        return new CompanyDto(company.getId(), company.getName(), addressDto, phoneNumberDto, company.getLogoUrl(),
                company.getLandingPrompt(), company.getTextColor(), company.getBackgroundColor(),
                company.getPrimaryButtonColor(), company.getSecondaryButtonColor(), company.getWarningButtonColor(),
                company.getDangerButtonColor());
    }

    public Company map(CompanyDto dto) {
        return new Company(dto.id(), dto.name(), dto.logoUrl(), dto.landingPrompt(), dto.textColor(),
                dto.backgroundColor(), dto.primaryButtonColor(), dto.secondaryButtonColor(),
                dto.warningButtonColor(), dto.dangerButtonColor());
    }
}
