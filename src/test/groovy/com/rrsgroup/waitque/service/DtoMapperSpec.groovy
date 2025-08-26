package com.rrsgroup.waitque.service

import com.rrsgroup.waitque.domain.SortDirection
import com.rrsgroup.waitque.dto.AddressDto
import com.rrsgroup.waitque.dto.CompanyDto
import com.rrsgroup.waitque.dto.PhoneNumberDto
import com.rrsgroup.waitque.entity.Address
import com.rrsgroup.waitque.entity.Company
import com.rrsgroup.waitque.entity.PhoneNumber
import com.rrsgroup.waitque.util.MockGenerator
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import spock.lang.Specification

class DtoMapperSpec extends Specification {
    def mockGenerator = new MockGenerator()
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
        def company = mockGenerator.getCompanyMock()

        when:
        def result = mapper.map(company)

        then:
        result.id() == company.id
        result.name() == company.name
        result.address().id() == company.address.id
        result.address().address1() == company.address.address1
        result.address().address2() == company.address.address2
        result.address().city() == company.address.city
        result.address().state() == company.address.state
        result.address().zipcode() == company.address.zipcode
        result.address().country() == company.address.country
        result.phoneNumber().id() == company.phoneNumber.id
        result.phoneNumber().countryCode() == company.phoneNumber.countryCode
        result.phoneNumber().phoneNumber() == company.phoneNumber.phoneNumber
        result.logoUrl() == company.logoUrl
        result.landingPrompt() == company.landingPrompt
        result.textColor() == company.textColor
        result.backgroundColor() == company.backgroundColor
        result.primaryButtonColor() == company.primaryButtonColor
        result.secondaryButtonColor() == company.secondaryButtonColor
        result.warningButtonColor() == company.warningButtonColor
        result.dangerButtonColor() == company.dangerButtonColor
    }

    def "can map from Page of Companies to CompanyListDto"() {
        given:
        Page<Company> pageOfCompanies = Mock()
        def company = mockGenerator.getCompanyMock()

        when:
        def result = mapper.map(pageOfCompanies)

        then:
        4 * pageOfCompanies.getPageable() >> pageable
        1 * pageOfCompanies.getTotalElements() >> 1
        1 * pageOfCompanies.getContent() >> List.of(company)
        0 * _

        result.getPage() == page
        result.getLimit() == limit
        result.getTotal() == 1
        result.getSortField() == sortField
        result.getSortDir() == sortDir
        result.getCompanies().get(0).getId() == company.getId()
        result.getCompanies().get(0).getName() == company.getName()
        result.getCompanies().get(0).getLogoUrl() == company.getLogoUrl()

        where:
        sortDir            | limit | page | sortField | pageable                                                     | description
        SortDirection.ASC  | 10    | 0    | "id"      | PageRequest.of(page, limit, Sort.by(sortField).ascending())  | "ascending"
        SortDirection.DESC | 10    | 0    | "id"      | PageRequest.of(page, limit, Sort.by(sortField).descending()) | "descending"
    }
}
