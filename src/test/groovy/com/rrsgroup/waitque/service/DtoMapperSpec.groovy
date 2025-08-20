package com.rrsgroup.waitque.service

import com.rrsgroup.waitque.dto.AddressDto
import com.rrsgroup.waitque.dto.PhoneNumberDto
import com.rrsgroup.waitque.entity.Address
import com.rrsgroup.waitque.entity.PhoneNumber
import spock.lang.Specification

class DtoMapperSpec extends Specification {
    def mapper = new DtoMapper()

    def "can map from AddressDto to Address"() {
        given:
        def id = 123L
        def address1 = "address1"
        def address2 = "address2"
        def city = "city"
        def state = "state"
        def zipcode = "zipcode"
        def country = "country"
        def dto = new AddressDto(id, address1, address2, city, state, zipcode, country)

        when:
        def result = mapper.map(dto)

        then:
        result.getId() == id
        result.getAddress1() == address1
        result.getAddress2() == address2
        result.getCity() == city
        result.getState() == state
        result.getZipcode() == zipcode
        result.getCountry() == country
    }

    def "can map from Address to AddressDto"() {
        given:
        def id = 123L
        def address1 = "address1"
        def address2 = "address2"
        def city = "city"
        def state = "state"
        def zipcode = "zipcode"
        def country = "country"
        def address = new Address(id, address1, address2, city, state, zipcode, country)

        when:
        def result = mapper.map(address)

        then:
        result.id() == id
        result.address1() == address1
        result.address2() == address2
        result.city() == city
        result.state() == state
        result.zipcode() == zipcode
        result.country() == country
    }

    def "can map from PhoneNumberDto to PhoneNumber"() {
        given:
        def id = 123L
        def countryCode = 1
        def phoneNumber = 1234567
        def dto = new PhoneNumberDto(id, countryCode, phoneNumber)

        when:
        def result = mapper.map(dto)

        then:
        result.getId() == id
        result.getCountryCode() == countryCode
        result.getPhoneNumber() == phoneNumber
    }

    def "can map from PhoneNumber to PhoneNumberDto"() {
        given:
        def id = 123L
        def countryCode = 1
        def phoneNumber = 1234567
        def phoneNumberDomain = new PhoneNumber(id, countryCode, phoneNumber)

        when:
        def result = mapper.map(phoneNumberDomain)

        then:
        result.id() == id
        result.countryCode() == countryCode
        result.phoneNumber() == phoneNumber
    }
}
