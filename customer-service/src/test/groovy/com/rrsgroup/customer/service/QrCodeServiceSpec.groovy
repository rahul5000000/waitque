package com.rrsgroup.customer.service

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
}
