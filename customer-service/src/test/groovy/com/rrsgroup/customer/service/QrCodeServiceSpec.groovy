package com.rrsgroup.customer.service

import com.rrsgroup.common.dto.CompanyUserDto
import com.rrsgroup.common.exception.IllegalUpdateException
import com.rrsgroup.customer.entity.Customer
import com.rrsgroup.customer.entity.QrCode
import com.rrsgroup.customer.repository.QrCodeRepository
import spock.lang.Specification

class QrCodeServiceSpec extends Specification {

    def qrCodeRepository = Mock(QrCodeRepository)
    def qrCodeService = new QrCodeService(qrCodeRepository)

    def "generateUnassignedQrCodes should create and save the correct number of QR codes"() {
        given:
        def count = 3
        def companyId = 99L
        def createdBy = "user123"

        when:
        def result = qrCodeService.generateUnassignedQrCodes(count, companyId, createdBy)

        then:
        1 * qrCodeRepository.saveAllAndFlush({ List<QrCode> qrCodes ->
            qrCodes.size() == 3 &&
                    qrCodes.every { it.companyId == 99L } &&
                    qrCodes.every { it.createdBy == "user123" && it.updatedBy == "user123" } &&
                    qrCodes.every { it.qrCode != null } &&
                    qrCodes.every { it.createdDate != null && it.updatedDate != null }
        }) >> { args -> args[0] } // return the same list back

        result.size() == 3
        result.every { it instanceof QrCode }
    }

    def "generateUnassignedQrCodes should handle zero count gracefully"() {
        when:
        def result = qrCodeService.generateUnassignedQrCodes(0, 1L, "user")

        then:
        1 * qrCodeRepository.saveAllAndFlush([]) >> []
        result.isEmpty()
    }

    def "getQrCodesForCustomers should delegate to repository"() {
        given:
        def customers = [new Customer(id: 1L), new Customer(id: 2L)]
        def expected = [new QrCode(id: 1L), new QrCode(id: 2L)]

        when:
        def result = qrCodeService.getQrCodesForCustomers(customers)

        then:
        1 * qrCodeRepository.findAllByCustomerIn(customers) >> expected
        result == expected
    }

    def "getQrCode should delegate to repository with correct parameters"() {
        given:
        def uuid = UUID.randomUUID()
        def user = Mock(CompanyUserDto) {
            getCompanyId() >> 77L
        }
        def qrCode = new QrCode(id: 1L, qrCode: uuid)

        when:
        def result = qrCodeService.getQrCode(uuid, user)

        then:
        1 * qrCodeRepository.findByQrCodeAndCompanyId(uuid, 77L) >> Optional.of(qrCode)
        result.isPresent()
        result.get() == qrCode
    }

    def "getQrCodeForCustomer should delegate to repository"() {
        given:
        def customer = new Customer(id: 5L)
        def qrCode = new QrCode(id: 1L, customer: customer)

        when:
        def result = qrCodeService.getQrCodeForCustomer(customer)

        then:
        1 * qrCodeRepository.findByCustomer(customer) >> Optional.of(qrCode)
        result.isPresent()
        result.get() == qrCode
    }

    def "associateQrCodeToCustomer should save QR code if unassigned"() {
        given:
        def customer = new Customer(id: 10L)
        def qrCode = new QrCode(id: 1L, customer: null)
        def existing = new QrCode(id: 1L, customer: null)

        when:
        def result = qrCodeService.associateQrCodeToCustomer(qrCode, customer)

        then:
        1 * qrCodeRepository.findById(1L) >> Optional.of(existing)
        1 * qrCodeRepository.save({ QrCode saved ->
            saved.customer == customer
        }) >> { args -> args[0] }

        result.customer == customer
    }

    def "associateQrCodeToCustomer should not throw if already assigned to same customer"() {
        given:
        def customer = new Customer(id: 10L)
        def existing = new QrCode(id: 1L, customer: customer)
        def qrCode = new QrCode(id: 1L, customer: customer)

        when:
        def result = qrCodeService.associateQrCodeToCustomer(qrCode, customer)

        then:
        1 * qrCodeRepository.findById(1L) >> Optional.of(existing)
        1 * qrCodeRepository.save(qrCode) >> qrCode
        result == qrCode
    }

    def "associateQrCodeToCustomer should throw if assigned to different customer"() {
        given:
        def existingCustomer = new Customer(id: 1L)
        def newCustomer = new Customer(id: 2L)
        def qrCode = new QrCode(id: 10L)
        def currentState = new QrCode(id: 10L, customer: existingCustomer)

        when:
        qrCodeService.associateQrCodeToCustomer(qrCode, newCustomer)

        then:
        1 * qrCodeRepository.findById(10L) >> Optional.of(currentState)
        0 * qrCodeRepository.save(_)
        def ex = thrown(IllegalUpdateException)
        ex.message == "The QR code is already assigned to another customer"
    }
}
