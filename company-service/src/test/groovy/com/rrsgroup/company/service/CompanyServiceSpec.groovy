package com.rrsgroup.company.service

import com.rrsgroup.common.domain.SortDirection
import com.rrsgroup.common.dto.SuperUserDto
import com.rrsgroup.common.dto.UserDto
import com.rrsgroup.common.entity.Address
import com.rrsgroup.common.entity.PhoneNumber
import com.rrsgroup.common.exception.IllegalUpdateException
import com.rrsgroup.company.entity.Company
import com.rrsgroup.company.entity.CompanyEmail
import com.rrsgroup.company.repository.CompanyEmailRepository
import com.rrsgroup.company.repository.CompanyRepository
import com.rrsgroup.company.util.CompanyMockGenerator
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import spock.lang.Specification

class CompanyServiceSpec extends Specification {
    def repository = Mock(CompanyRepository)
    def companyEmailRepository = Mock(CompanyEmailRepository)
    def companyService = new CompanyService(repository, companyEmailRepository)
    def companyMockGenerator = new CompanyMockGenerator()

    def "createCompany saves the company via the repository and returns saved company"() {
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

        def company = new Company(null, name, logoUrl, landingPrompt, textColor, backgroundColor, primaryButtonColor,
                secondaryButtonColor, warningButtonColor, dangerButtonColor, address, phoneNumberDomain, new ArrayList<CompanyEmail>())

        def savedCompany = new Company(companyId, name, logoUrl, landingPrompt, textColor, backgroundColor, primaryButtonColor,
                secondaryButtonColor, warningButtonColor, dangerButtonColor, address, phoneNumberDomain, new ArrayList<CompanyEmail>())

        def user = Mock(SuperUserDto.class) {
            getUserId() >> "userId"
        }

        when:
        def result = companyService.createCompany(company, user)

        then:
        1 * repository.save(company) >> savedCompany
        0 * _
        result == savedCompany
    }

    def "getListOfCompanies finds all companies with pagination parameters #description"() {
        when:
        companyService.getListOfCompanies(limit, page, sortField, sortDir)

        then:
        1 * repository.findAll(pageable)
        0 * _

        where:
        sortDir            | limit | page | sortField | pageable                                                     | description
        SortDirection.ASC | 10 | 0 | "id" | PageRequest.of(page, limit, Sort.by(sortField).ascending()) | "ascending"
        SortDirection.DESC | 10    | 0    | "id"      | PageRequest.of(page, limit, Sort.by(sortField).descending()) | "descending"
    }

    def "getCompany finds specific company"() {
        given:
        def company = companyMockGenerator.getCompanyMock()
        def companyId = company.getId()

        when:
        def result = companyService.getCompany(companyId)

        then:
        1 * repository.findById(companyId) >> Optional.of(company)
        0 * _
        result.isPresent()
        result.get().id == companyId
    }

    def "updateCompany updates a company and returns the updated record"() {
        given:
        def company = companyMockGenerator.getCompanyMock()
        def updatedCompany = companyMockGenerator.getCompanyMock()
        updatedCompany.setName(company.getName() + "Updated")
        def user = Mock(UserDto.class) {
            getUserId() >> "userId"
        }

        when:
        def result = companyService.updateCompany(company, user)

        then:
        1 * repository.save(company) >> updatedCompany
        1 * repository.findById(1231) >> Optional.of(company)
        0 * _
        result != null
        result.getName() == company.getName() + "Updated"
    }

    def "updateCompany throws an exception if the request does not have an ID"() {
        given:
        def company = companyMockGenerator.getCompanyMock()
        company.setId(null)
        def user = Mock(UserDto.class) {
            getUserId() >> "userId"
        }

        when:
        def result = companyService.updateCompany(company, user)

        then:
        thrown(IllegalUpdateException)
    }
}
