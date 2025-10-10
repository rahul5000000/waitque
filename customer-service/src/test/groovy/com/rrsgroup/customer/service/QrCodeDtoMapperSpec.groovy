package com.rrsgroup.customer.service

import com.rrsgroup.customer.entity.Customer
import com.rrsgroup.customer.entity.QrCode
import spock.lang.Specification

import java.time.LocalDateTime

class QrCodeDtoMapperSpec extends Specification {
    def mapper = new QrCodeDtoMapper()

    def "List of QrCode maps to List of QrCodeDto"() {
        given:
        def companyId = 1L
        def now = LocalDateTime.now();
        def userId = "1"
        def customer1 = Mock(Customer) {
            getId() >> 1L
        }

        def qrCode1 = UUID.randomUUID()
        def qrCode2 = UUID.randomUUID()

        def qrCodes = List.of(
                new QrCode(1L, companyId, customer1, qrCode1, now, now, userId, userId),
                new QrCode(2L, companyId, null, qrCode2, now, now, userId, userId))

        when:
        def result = mapper.map(qrCodes)

        then:
        result.size() == 2
        result*.id() == [1L, 2L]
        result*.companyId() == [1L,1L]
        result*.customerId() == [1L, null]
        result*.qrCode() == [qrCode1, qrCode2]
        result*.createdDate() == [now, now]
        result*.updatedDate() == [now, now]
        result*.createdBy() == [userId, userId]
        result*.updatedBy() == [userId, userId]
    }
}
