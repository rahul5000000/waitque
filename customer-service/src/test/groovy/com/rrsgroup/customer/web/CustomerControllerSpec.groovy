package com.rrsgroup.customer.web

import com.rrsgroup.common.dto.FieldUserDto
import com.rrsgroup.common.exception.IllegalUpdateException
import com.rrsgroup.common.exception.RecordNotFoundException
import com.rrsgroup.customer.dto.AssociateQrCodeDto
import com.rrsgroup.customer.dto.CustomerDto
import com.rrsgroup.customer.entity.Customer
import com.rrsgroup.customer.entity.QrCode
import com.rrsgroup.customer.service.CompanyService
import com.rrsgroup.customer.service.CrmCustomerDtoMapper
import com.rrsgroup.customer.service.CustomerCrmIntegrationService
import com.rrsgroup.customer.service.CustomerDtoMapper
import com.rrsgroup.customer.service.CustomerService
import com.rrsgroup.customer.service.EventService
import com.rrsgroup.customer.service.LeadFlowService
import com.rrsgroup.customer.service.QrCodeService
import spock.lang.Specification

class CustomerControllerSpec extends Specification {

    def crmCustomerDtoMapper = Mock(CrmCustomerDtoMapper)
    def integrationService = Mock(CustomerCrmIntegrationService)
    def customerService = Mock(CustomerService)
    def qrCodeService = Mock(QrCodeService)
    def customerDtoMapper = Mock(CustomerDtoMapper)
    def companyService = Mock(CompanyService)
    def leadFlowService = Mock(LeadFlowService)
    def eventService = Mock(EventService)

    def controller = new CustomerController(
            crmCustomerDtoMapper,
            integrationService,
            customerService,
            qrCodeService,
            customerDtoMapper,
            companyService,
            leadFlowService,
            eventService
    )

    def fieldUserDto = Mock(FieldUserDto) {
        getCompanyId() >> 101L
    }
    def customer = new Customer(id: 1L)
    def qrCode = new QrCode(id: 5L, qrCode: UUID.randomUUID())

    // --- 1. Customer not found ---
    def "should throw RecordNotFoundException when customer not found"() {
        given:
        def request = new AssociateQrCodeDto(qrCode.qrCode)

        when:
        controller.associateQrCodeWithCustomer(fieldUserDto, 1L, request)

        then:
        1 * customerService.getCustomerById(1L, fieldUserDto) >> Optional.empty()
        def ex = thrown(RecordNotFoundException)
        ex.message == "Customer not found with customerId=1"
        0 * _
    }

    // --- 2. QR code not found ---
    def "should throw RecordNotFoundException when QR code not found"() {
        given:
        def request = new AssociateQrCodeDto(qrCode.qrCode)

        when:
        controller.associateQrCodeWithCustomer(fieldUserDto, 1L, request)

        then:
        1 * customerService.getCustomerById(1L, fieldUserDto) >> Optional.of(customer)
        1 * qrCodeService.getQrCode(request.qrCode(), fieldUserDto) >> Optional.empty()
        def ex = thrown(RecordNotFoundException)
        ex.message.contains("QR code not found")
        0 * _
    }

    // --- 3. Customer already associated to same QR code ---
    def "should return existing mapping when duplicate associate request"() {
        given:
        def request = new AssociateQrCodeDto(qrCode.qrCode)
        def existingQr = new QrCode(id: 5L, qrCode: qrCode.qrCode, customer: customer)
        def expectedDto = new CustomerDto(1L, "CRM123", qrCode.qrCode)

        when:
        def result = controller.associateQrCodeWithCustomer(fieldUserDto, 1L, request)

        then:
        1 * customerService.getCustomerById(1L, fieldUserDto) >> Optional.of(customer)
        1 * qrCodeService.getQrCode(request.qrCode(), fieldUserDto) >> Optional.of(qrCode)
        1 * qrCodeService.getQrCodeForCustomer(customer) >> Optional.of(existingQr)
        1 * customerDtoMapper.map(existingQr) >> expectedDto
        result == expectedDto
        0 * _
    }

    // --- 4. Customer already associated to another QR code ---
    def "should throw IllegalUpdateException when customer is associated to another QR code"() {
        given:
        def request = new AssociateQrCodeDto(UUID.randomUUID())
        def existingQr = new QrCode(id: 99L, qrCode: UUID.randomUUID(), customer: customer)

        when:
        controller.associateQrCodeWithCustomer(fieldUserDto, 1L, request)

        then:
        1 * customerService.getCustomerById(1L, fieldUserDto) >> Optional.of(customer)
        1 * qrCodeService.getQrCode(request.qrCode(), fieldUserDto) >> Optional.of(qrCode)
        1 * qrCodeService.getQrCodeForCustomer(customer) >> Optional.of(existingQr)
        def ex = thrown(IllegalUpdateException)
        ex.message == "The customer is already associated with another QR code"
        0 * _
    }

    // --- 5. QR code already assigned to another customer ---
    def "should throw IllegalUpdateException when QR code already assigned to another customer"() {
        given:
        def request = new AssociateQrCodeDto(qrCode.qrCode)
        def anotherCustomer = new Customer(id: 2L)
        qrCode.customer = anotherCustomer

        when:
        controller.associateQrCodeWithCustomer(fieldUserDto, 1L, request)

        then:
        1 * customerService.getCustomerById(1L, fieldUserDto) >> Optional.of(customer)
        1 * qrCodeService.getQrCode(request.qrCode(), fieldUserDto) >> Optional.of(qrCode)
        1 * qrCodeService.getQrCodeForCustomer(customer) >> Optional.empty()
        def ex = thrown(IllegalUpdateException)
        ex.message == "The QR code is already associated with another customer"
        0 * _
    }

    // --- 6. Success path ---
    def "should associate QR code to customer successfully"() {
        given:
        def request = new AssociateQrCodeDto(qrCode.qrCode)
        def savedQr = new QrCode(id: 5L, qrCode: qrCode.qrCode, customer: customer)
        def expectedDto = new CustomerDto(1L, "CRM-999", qrCode.qrCode)

        when:
        def result = controller.associateQrCodeWithCustomer(fieldUserDto, 1L, request)

        then:
        1 * customerService.getCustomerById(1L, fieldUserDto) >> Optional.of(customer)
        1 * qrCodeService.getQrCode(request.qrCode(), fieldUserDto) >> Optional.of(qrCode)
        1 * qrCodeService.getQrCodeForCustomer(customer) >> Optional.empty()
        1 * qrCodeService.associateQrCodeToCustomer(qrCode, customer) >> savedQr
        1 * customerDtoMapper.map(savedQr) >> expectedDto

        result == expectedDto
    }
}
