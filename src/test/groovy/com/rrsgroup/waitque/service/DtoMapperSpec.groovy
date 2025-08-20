package com.rrsgroup.waitque.service

import com.rrsgroup.waitque.dto.AddressDto
import com.rrsgroup.waitque.dto.CompanyDto
import com.rrsgroup.waitque.dto.PhoneNumberDto
import com.rrsgroup.waitque.entity.Address
import com.rrsgroup.waitque.entity.Company
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

    def "can map from CompanyDto to Company"() {
        given:
        def companyId = 1231L
        def name = "name"
        def logoUrl = "logoUrl.com"
        def landingPrompt = "What do you want to do?"
        def textColor = "#FFFFFF"
        def backgroundColor = "#000000"
        def primaryButtonColor = "#AAAAAA"
        def secondaryButtonColor = "#BBBBBB"
        def warningButtonColor = "#CCCCCC"
        def dangerButtonColor = "#DDDDDD"

        def addressId = 1232L
        def address1 = "address1"
        def address2 = "address2"
        def city = "city"
        def state = "state"
        def zipcode = "zipcode"
        def country = "country"

        def phoneNumberId = 1233L
        def countryCode = 1
        def phoneNumber = 1234567

        def addressDto = new AddressDto(addressId, address1, address2, city, state, zipcode, country)
        def phoneNumberDto = new PhoneNumberDto(phoneNumberId, countryCode, phoneNumber)

        def dto = new CompanyDto(companyId, name, addressDto, phoneNumberDto, logoUrl, landingPrompt, textColor,
                backgroundColor, primaryButtonColor, secondaryButtonColor, warningButtonColor, dangerButtonColor)

        when:
        def result = mapper.map(dto)

        then:
        result.getId() == companyId
        result.getName() == name
        result.getLogoUrl() == logoUrl
        result.getLandingPrompt() == landingPrompt
        result.getTextColor() == textColor
        result.getBackgroundColor() == backgroundColor
        result.getPrimaryButtonColor() == primaryButtonColor
        result.getSecondaryButtonColor() == secondaryButtonColor
        result.getWarningButtonColor() == warningButtonColor
        result.getDangerButtonColor() == dangerButtonColor
    }

    def "can map from Company, Address & PhoneNumber to CompanyDto"() {
        given:
        def companyId = 1231L
        def name = "name"
        def logoUrl = "logoUrl.com"
        def landingPrompt = "What do you want to do?"
        def textColor = "#FFFFFF"
        def backgroundColor = "#000000"
        def primaryButtonColor = "#AAAAAA"
        def secondaryButtonColor = "#BBBBBB"
        def warningButtonColor = "#CCCCCC"
        def dangerButtonColor = "#DDDDDD"

        def addressId = 1232L
        def address1 = "address1"
        def address2 = "address2"
        def city = "city"
        def state = "state"
        def zipcode = "zipcode"
        def country = "country"

        def phoneNumberId = 1233L
        def countryCode = 1
        def phoneNumber = 1234567

        def address = new Address(addressId, address1, address2, city, state, zipcode, country)
        def phoneNumberDomain = new PhoneNumber(phoneNumberId, countryCode, phoneNumber)

        def company = new Company(companyId, name, logoUrl, landingPrompt, textColor, backgroundColor, primaryButtonColor,
            secondaryButtonColor, warningButtonColor, dangerButtonColor)

        when:
        def result = mapper.map(company, address, phoneNumberDomain)

        then:
        result.id() == companyId
        result.name() == name
        result.address().id() == addressId
        result.address().address1() == address1
        result.address().address2() == address2
        result.address().city() == city
        result.address().state() == state
        result.address().zipcode() == zipcode
        result.address().country() == country
        result.phoneNumber().id() == phoneNumberId
        result.phoneNumber().countryCode() == countryCode
        result.phoneNumber().phoneNumber() == phoneNumber
        result.logoUrl() == logoUrl
        result.landingPrompt() == landingPrompt
        result.textColor() == textColor
        result.backgroundColor() == backgroundColor
        result.primaryButtonColor() == primaryButtonColor
        result.secondaryButtonColor() == secondaryButtonColor
        result.warningButtonColor() == warningButtonColor
        result.dangerButtonColor() == dangerButtonColor
    }
}
