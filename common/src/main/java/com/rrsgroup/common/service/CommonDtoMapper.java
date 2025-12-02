package com.rrsgroup.common.service;

import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.EmailDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.common.entity.Address;
import com.rrsgroup.common.entity.Email;
import com.rrsgroup.common.entity.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class CommonDtoMapper {
    public AddressDto map(final Address address) {
        return new AddressDto(
                address.getId(),
                address.getAddress1(),
                address.getAddress2(),
                address.getCity(),
                address.getState(),
                address.getZipcode(),
                address.getCountry());
    }

    public Address map(final AddressDto dto) {
        return new Address(
                dto.id(),
                dto.address1(),
                dto.address2(),
                dto.city(),
                dto.state(),
                dto.zipcode(),
                dto.country());
    }

    public PhoneNumberDto map(final PhoneNumber phoneNumber) {
        return new PhoneNumberDto(phoneNumber.getId(), phoneNumber.getCountryCode(), phoneNumber.getPhoneNumber());
    }
    
    public PhoneNumber map(final PhoneNumberDto dto) {
        return new PhoneNumber(dto.id(), dto.countryCode(), dto.phoneNumber());
    }

    public EmailDto map(final Email email) {
        return new EmailDto(email.getEmail(), email.getFirstName(), email.getLastName());
    }

    public Email map(final EmailDto dto) {
        return Email.builder().email(dto.email()).firstName(dto.firstName()).lastName(dto.lastName()).build();
    }
}
