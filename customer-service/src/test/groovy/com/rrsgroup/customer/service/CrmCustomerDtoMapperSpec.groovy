package com.rrsgroup.customer.service

import com.rrsgroup.customer.domain.CrmAddress
import com.rrsgroup.customer.domain.CrmCustomer
import com.rrsgroup.customer.domain.CrmCustomerType
import com.rrsgroup.customer.domain.CrmPhoneNumber
import com.rrsgroup.customer.domain.CustomerSearchResult
import com.rrsgroup.customer.entity.Customer
import spock.lang.Specification

class CrmCustomerDtoMapperSpec extends Specification {

    def mapper = new CrmCustomerDtoMapper()

    def "should map list of CustomerSearchResult to CustomersSearchResultDto"() {
        given:
        def crmAddress = new CrmAddress("123 Main St.", null, "Atlanta", "Georgia", "30303", "USA")
        def crmPhoneNumber = new CrmPhoneNumber(1, 1231231234)
        def crmCustomer1 = new CrmCustomer(CrmCustomerType.RESIDENTIAL, "crm-123", null, "John", "Doe", crmAddress, crmPhoneNumber, "john.d@test.com")
        def crmCustomer2 = new CrmCustomer(CrmCustomerType.RESIDENTIAL, "crm-456", null, "Jane", "Smith", crmAddress, crmPhoneNumber, "jane.s@test.com")

        def customer1 = new Customer(id: 1L)
        def customer2 = new Customer(id: 2L)

        def searchResult1 = new CustomerSearchResult(crmCustomer1, customer1, false)
        def searchResult2 = new CustomerSearchResult(crmCustomer2, customer2, true)

        def searchResults = [searchResult1, searchResult2]

        when:
        def resultDto = mapper.map(searchResults)

        then:
        resultDto.customers.size() == 2

        with(resultDto.customers.get(0)) {
            id == 1L
            firstName == "John"
            lastName == "Doe"
            crmCustomerId == "crm-123"
            isAssociated == false
        }

        with(resultDto.customers.get(1)) {
            id == 2L
            firstName == "Jane"
            lastName == "Smith"
            crmCustomerId == "crm-456"
            isAssociated == true
        }
    }

    def "should handle empty list gracefully"() {
        when:
        def resultDto = mapper.map([])

        then:
        resultDto.customers.isEmpty()
    }
}
