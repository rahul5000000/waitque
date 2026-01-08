package com.rrsgroup.customer.service

import com.rrsgroup.common.dto.CompanyUserDto
import com.rrsgroup.customer.domain.CrmAddress
import com.rrsgroup.customer.domain.CrmCustomer
import com.rrsgroup.customer.domain.CrmCustomerType
import com.rrsgroup.customer.domain.CrmPhoneNumber
import com.rrsgroup.customer.entity.CrmConfig
import com.rrsgroup.customer.entity.Customer
import com.rrsgroup.customer.repository.CustomerCodeRespository
import com.rrsgroup.customer.repository.CustomerRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class CustomerServiceSpec extends Specification {

    def customerRepository = Mock(CustomerRepository)
    def qrCodeService = Mock(QrCodeService)
    def customerCodeRespository = Mock(CustomerCodeRespository)

    @Subject
    def service = new CustomerService(customerRepository, qrCodeService, customerCodeRespository)

    def "should create a new customer and save it with correct fields"() {
        given:
        def crmConfig = new CrmConfig(id: 100L)
        def crmAddress = new CrmAddress("123 Main St.", null, "Atlanta", "Georgia", "30303", "USA")
        def crmPhoneNumber = new CrmPhoneNumber(1, 1231231234)
        def crmCustomer = new CrmCustomer(CrmCustomerType.RESIDENTIAL, "crm-123", null, "John", "Doe", crmAddress, crmPhoneNumber, "john.d@test.com")
        def companyUser = Mock(CompanyUserDto) {
            getUserId() >> "user-1"
        }

        and: "repository returns saved entity"
        def savedCustomer = new Customer(id: 1L, crmCustomerId: "crm-123")
        1 * customerRepository.save(_ as Customer) >> { Customer c ->
            assert c.crmCustomerId == "crm-123"
            assert c.crmConfig == crmConfig
            assert c.createdBy == "user-1"
            assert c.updatedBy == "user-1"
            assert c.createdDate instanceof LocalDateTime
            assert c.updatedDate instanceof LocalDateTime
            return savedCustomer
        }

        and: "random customer code is generated"
        1 * customerCodeRespository.findByCustomerCode(_) >> Optional.empty()

        when:
        def result = service.createCustomer(crmConfig, crmCustomer, companyUser)

        then:
        result.id == 1L
        result.crmCustomerId == "crm-123"
    }

    def "should get customer by crmConfig and crmCustomer"() {
        given:
        def crmConfig = new CrmConfig(id: 10L)
        def crmAddress = new CrmAddress("123 Main St.", null, "Atlanta", "Georgia", "30303", "USA")
        def crmPhoneNumber = new CrmPhoneNumber(1, 1231231234)
        def crmCustomer = new CrmCustomer(CrmCustomerType.RESIDENTIAL, "crm-456", null, "Jane", "Smith", crmAddress, crmPhoneNumber, "jane.s@test.com")
        def expectedCustomer = new Customer(id: 99L, crmCustomerId: "crm-456")

        and:
        1 * customerRepository.findByCrmCustomerIdAndCrmConfig_Id("crm-456", 10L) >> expectedCustomer

        when:
        def result = service.getCustomerByCrmConfig(crmConfig, crmCustomer)

        then:
        result == expectedCustomer
    }

    def "should get list of customers by crmConfig and crmCustomers"() {
        given:
        def crmConfig = new CrmConfig(id: 50L)
        def crmAddress = new CrmAddress("123 Main St.", null, "Atlanta", "Georgia", "30303", "USA")
        def crmPhoneNumber = new CrmPhoneNumber(1, 1231231234)
        def crmCustomers = [
                new CrmCustomer(CrmCustomerType.RESIDENTIAL, "crm-1", null, "A", "B", crmAddress, crmPhoneNumber, "a.b@test.com"),
                new CrmCustomer(CrmCustomerType.RESIDENTIAL, "crm-2", null, "C", "D", crmAddress, crmPhoneNumber, "c.d@test.com")
        ]
        def expectedCustomers = [
                new Customer(id: 1L, crmCustomerId: "crm-1"),
                new Customer(id: 2L, crmCustomerId: "crm-2")
        ]

        and:
        1 * customerRepository.findAllByCrmCustomerIdInAndCrmConfig_Id(["crm-1", "crm-2"], 50L) >> expectedCustomers

        when:
        def result = service.getCustomersByCrmConfig(crmConfig, crmCustomers)

        then:
        result == expectedCustomers
        result*.crmCustomerId == ["crm-1", "crm-2"]
    }

    def "getCustomerById should invoke repository method"() {
        given:
        def customerId = 1L
        def userDto = Mock(CompanyUserDto) {
            getCompanyId() >> 2L
        }

        when:
        service.getCustomerById(customerId, userDto)

        then:
        1 * customerRepository.findByIdAndCrmConfig_CompanyId(customerId, 2L);
    }
}
