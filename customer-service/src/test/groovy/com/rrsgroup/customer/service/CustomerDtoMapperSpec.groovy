package com.rrsgroup.customer.service

import com.rrsgroup.customer.entity.Customer
import com.rrsgroup.customer.entity.QrCode
import spock.lang.Specification

class CustomerDtoMapperSpec extends Specification {

    def mapper = new CustomerDtoMapper()

    def "map should correctly map QrCode with Customer"() {
        given:
        def customer = new Customer(id: 42L, crmCustomerId: "CRM-001")
        def qrCode = new QrCode(qrCode: UUID.randomUUID(), customer: customer)

        when:
        def dto = mapper.map(qrCode)

        then:
        dto.id == 42L
        dto.crmCustomerId == "CRM-001"
        dto.qrCode == qrCode.qrCode
    }

    def "map should handle null Customer gracefully"() {
        given:
        def qrCode = new QrCode(qrCode: UUID.randomUUID(), customer: null)

        when:
        def dto = mapper.map(qrCode)

        then:
        dto.id == null
        dto.crmCustomerId == null
        dto.qrCode == qrCode.qrCode
    }
}