package com.rrsgroup.company.web

import com.rrsgroup.common.domain.SortDirection
import com.rrsgroup.common.domain.UserRole
import com.rrsgroup.common.dto.AddressDto
import com.rrsgroup.common.dto.AdminUserDto
import com.rrsgroup.common.dto.PhoneNumberDto
import com.rrsgroup.common.exception.IllegalRequestException
import com.rrsgroup.common.exception.IllegalUpdateException
import com.rrsgroup.common.exception.RecordNotFoundException
import com.rrsgroup.common.service.CommonDtoMapper
import com.rrsgroup.common.util.ImageWrapper
import com.rrsgroup.company.dto.CompanyDto
import com.rrsgroup.company.entity.Company
import com.rrsgroup.company.entity.QrCode
import com.rrsgroup.company.service.CompanyDtoMapper
import com.rrsgroup.company.service.CompanyService
import com.rrsgroup.company.service.FrontEndLinkService
import com.rrsgroup.company.service.QrCodeService
import com.rrsgroup.company.util.CommonMockGenerator
import com.rrsgroup.company.util.CompanyMockGenerator
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

import java.awt.image.BufferedImage

class CompanyControllerSpec extends Specification {
    def companyService = Mock(CompanyService)
    def commonDtoMapper = new CommonDtoMapper()
    def mapper = new CompanyDtoMapper(commonDtoMapper)
    def companyMockGenerator = new CompanyMockGenerator()
    def commonMockGenerator = new CommonMockGenerator()
    def qrCodeService = Mock(QrCodeService)
    def frontEndLinkService = Mock(FrontEndLinkService)

    def controller = new CompanyController(companyService, mapper, qrCodeService, frontEndLinkService)

    def "createCompany throws a BAD REQUEST exception for requests that contain IDs"() {
        given:
        def dto = buildCompanyDto(companyId, name)

        when:
        controller.createCompany(dto)

        then:
        def e = thrown(IllegalRequestException.class)

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
        def company = companyMockGenerator.getCompanyMock()

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

    def "getCompany returns company for a valid companyId"() {
        given:
        def company = companyMockGenerator.getCompanyMock()
        def companyId = company.getId()

        when:
        def result = controller.getCompany(companyId)

        then:
        1 * companyService.getCompany(companyId) >> Optional.of(company)
        0 * _
        result != null
        result.id() == companyId
    }

    def "getCompany returns a 404 for a companyId that does not exist"() {
        given:
        def companyId = -1

        when:
        def result = controller.getCompany(companyId)

        then:
        1 * companyService.getCompany(companyId) >> Optional.empty()
        0 * _
        def e = thrown(RecordNotFoundException.class)
    }

    def "updateCompany returns a 400 if companyId in the URL and request body don't match"() {
        given:
        def companyId = 123L
        def name = "name"
        def dto = buildCompanyDto(companyId, name)

        when:
        controller.updateCompany(456L, dto)

        then:
        def e = thrown(IllegalRequestException.class)
    }

    def "updateCompany returns a 404 if companyId does not exist"() {
        given:
        def companyId = 123L
        def name = "name"
        def dto = buildCompanyDto(companyId, name)

        when:
        controller.updateCompany(companyId, dto)

        then:
        1 * companyService.getCompany(companyId) >> Optional.empty()
        0 * _
        def e = thrown(RecordNotFoundException.class)
    }

    def "updateCompany calls service to update the company details"() {
        given:
        def companyId = 1231L
        def name = "name"
        def dto = buildCompanyDto(companyId, name)
        def company = companyMockGenerator.getCompanyMock()
        def updateRequest = mapper.map(dto)
        def updatedCompany = companyMockGenerator.getCompanyMock()
        updatedCompany.setName(company.getName() + "Updated")

        when:
        def result = controller.updateCompany(companyId, dto)

        then:
        1 * companyService.getCompany(companyId) >> Optional.of(company)
        1 * companyService.updateCompany(updateRequest) >> updatedCompany
        0 * _
        result != null
        result.id() == companyId
        result.name() == company.getName() + "Updated"
    }

    def "updateCompany calls service to update the company details and sets the ID from the URL if the body doesn't have an ID"() {
        given:
        def companyId = 1231L
        def name = "name"
        def dto = buildCompanyDto(null, name)
        def company = companyMockGenerator.getCompanyMock()
        def updateRequest = mapper.map(buildCompanyDto(companyId, name))
        def updatedCompany = companyMockGenerator.getCompanyMock()
        updatedCompany.setName(company.getName() + "Updated")

        when:
        def result = controller.updateCompany(companyId, dto)

        then:
        1 * companyService.getCompany(companyId) >> Optional.of(company)
        1 * companyService.updateCompany(updateRequest) >> updatedCompany
        0 * _
        result != null
        result.id() == companyId
        result.name() == company.getName() + "Updated"
    }

    def "Admin getCompany returns company from companyId in principal"() {
        given:
        def principal = (AdminUserDto)commonMockGenerator.getUserMock(UserRole.ADMIN)
        def company = companyMockGenerator.getCompanyMock()
        def companyId = principal.getCompanyId()

        when:
        def result = controller.getCompany(principal)

        then:
        1 * companyService.getCompany(companyId) >> Optional.of(company)
        0 * _
        result != null
        result.id() == companyId
    }

    def "Admin updateCompany updates company from companyId in principal"() {
        given:
        def principal = (AdminUserDto)commonMockGenerator.getUserMock(UserRole.ADMIN)
        def companyId = principal.getCompanyId()
        def name = "name"
        def dto = buildCompanyDto(companyId, name)
        def company = companyMockGenerator.getCompanyMock()
        def updateRequest = mapper.map(dto)
        def updatedCompany = companyMockGenerator.getCompanyMock()
        updatedCompany.setName(company.getName() + "Updated")

        when:
        def result = controller.updateCompany(principal, dto)

        then:
        1 * companyService.getCompany(companyId) >> Optional.of(company)
        1 * companyService.updateCompany(updateRequest) >> updatedCompany
        0 * _
        result != null
        result.id() == companyId
        result.name() == company.getName() + "Updated"
    }

    def "Admin updateCompany returns 409 if the companyId in the body does not match principal's companyId"() {
        given:
        def principal = (AdminUserDto)commonMockGenerator.getUserMock(UserRole.ADMIN)
        def companyId = -1L
        def name = "name"
        def dto = buildCompanyDto(companyId, name)
        def company = companyMockGenerator.getCompanyMock()
        def updateRequest = mapper.map(dto)
        def updatedCompany = companyMockGenerator.getCompanyMock()
        updatedCompany.setName(company.getName() + "Updated")

        when:
        def result = controller.updateCompany(principal, dto)

        then:
        0 * _
        def e = thrown(IllegalUpdateException)
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

    def "generateAssignableQrCodes returns 400 if the count is less than 1"() {
        given:
        def principal = (AdminUserDto)commonMockGenerator.getUserMock(UserRole.ADMIN)
        def company = companyMockGenerator.getCompanyMock()
        def companyId = principal.getCompanyId()
        def height = 200, width = 200
        def response = Mock(HttpServletResponse.class)

        when:
        controller.generateAssignableQrCodes(principal, 0, height, width, response)

        then:
        1 * companyService.getCompany(companyId) >> Optional.of(company)
        thrown(IllegalRequestException.class)
    }

    def "generateAssignableQrCodes returns zip file of images"() {
        given:
        def principal = (AdminUserDto)commonMockGenerator.getUserMock(UserRole.ADMIN)
        def company = companyMockGenerator.getCompanyMock()
        def companyId = principal.getCompanyId()
        def height = 200, width = 200
        def baos = new ByteArrayOutputStream()
        def response = Mock(HttpServletResponse) {
            getOutputStream() >> new DelegatingServletOutputStream(baos)
        }
        def qrCodes = [
                QrCode.builder().qrCode(UUID.randomUUID()).company(company).build(),
                QrCode.builder().qrCode(UUID.randomUUID()).company(company).build()
        ]

        when:
        controller.generateAssignableQrCodes(principal, 2, height, width, response)

        then:
        1 * companyService.getCompany(companyId) >> Optional.of(company)
        1 * qrCodeService.generateQrCodes(2, company, principal) >> qrCodes
        2 * frontEndLinkService.getCustomerLandingPageLink(companyId, _) >> "test.com"
        2 * qrCodeService.generateQRCodeImage("test.com", width, height) >> Mock(ImageWrapper.class) {
            toByteArray("png") >> []
        }

        1 * response.setContentType("application/zip")
        1 * response.setHeader("Content-Disposition", _)
    }

    static class DelegatingServletOutputStream extends ServletOutputStream {
        private final OutputStream targetStream
        DelegatingServletOutputStream(OutputStream targetStream) {
            this.targetStream = targetStream
        }
        @Override
        void write(int b) throws IOException {
            targetStream.write(b)
        }

        @Override
        boolean isReady() {
            return false
        }

        @Override
        void setWriteListener(WriteListener writeListener) {

        }
    }
}
