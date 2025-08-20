package com.rrsgroup.waitque.service;

import com.rrsgroup.waitque.dto.AddressDto;
import com.rrsgroup.waitque.entity.Address;
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
}
