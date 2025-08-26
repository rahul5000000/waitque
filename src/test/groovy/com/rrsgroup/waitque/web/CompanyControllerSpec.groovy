package com.rrsgroup.waitque.web

import com.rrsgroup.waitque.domain.SortDirection
import com.rrsgroup.waitque.dto.AddressDto
import com.rrsgroup.waitque.dto.CompanyDto
import com.rrsgroup.waitque.dto.PhoneNumberDto
import com.rrsgroup.waitque.entity.Company
import com.rrsgroup.waitque.service.CompanyService
import com.rrsgroup.waitque.service.DtoMapper
import com.rrsgroup.waitque.util.MockGenerator
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class CompanyControllerSpec extends Specification {
    def companyService = Mock(CompanyService.class)
    def mapper = new DtoMapper()
    def mockGenerator = new MockGenerator()

    def controller = new CompanyController(companyService, mapper)

    def "createCompany throws a BAD REQUEST exception for requests that contain IDs"() {
        given:
        def dto = buildCompanyDto(companyId, name)

        when:
        controller.createCompany(dto)

        then:
        def e = thrown(ResponseStatusException.class)
        e.getStatusCode() == HttpStatus.BAD_REQUEST

        where:
        companyId | name   | description
        1231L     | "name" | "contain IDs"
        null      | null   | "do not contain name"
    }

    def "createCompany calls service to create company"() {
        given:
        def companyId = 1231L
        def name = "name"
        def dto = buildCompanyDto(null, name)
        def savedCompany = mapper.map(buildCompanyDto(companyId, name))

        when:
        def result = controller.createCompany(dto)

        then:
        1 * companyService.createCompany(_) >> savedCompany
        0 * _
        result != null
        result.id() == companyId
    }

    def "getListOfCompanies calls services to retrieve paginated companies"() {
        given:
        def limit = 10
        def page = 0
        def sortField = "id"
        def sortDir = SortDirection.ASC
        def pageable = PageRequest.of(page, limit, Sort.by(sortField).ascending())
        Page<Company> pageOfCompanies = Mock()
        def company = mockGenerator.getCompanyMock()

        when:
        def result = controller.getListOfCompanies(limit, page, sortField, sortDir)

        then:
        1 * companyService.getListOfCompanies(limit, page, sortField, sortDir) >> pageOfCompanies
        4 * pageOfCompanies.getPageable() >> pageable
        1 * pageOfCompanies.getTotalElements() >> 1
        1 * pageOfCompanies.getContent() >> List.of(company)
        0 * _
        result != null
        result.getPage() == page
        result.getLimit() == limit
        result.getTotal() == 1
        result.getSortField() == sortField
        result.getSortDir() == sortDir
        result.getCompanies().get(0).getId() == company.getId()
        result.getCompanies().get(0).getName() == company.getName()
        result.getCompanies().get(0).getLogoUrl() == company.getLogoUrl()
    }

    private static CompanyDto buildCompanyDto (Long companyId, String name) {
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

        return new CompanyDto(companyId, name, addressDto, phoneNumberDto, logoUrl, landingPrompt, textColor,
                backgroundColor, primaryButtonColor, secondaryButtonColor, warningButtonColor, dangerButtonColor)
    }
}
