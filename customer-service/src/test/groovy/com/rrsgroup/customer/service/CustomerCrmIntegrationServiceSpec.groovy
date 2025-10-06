package com.rrsgroup.customer.service

import com.rrsgroup.common.dto.FieldUserDto
import com.rrsgroup.customer.domain.CrmAddress
import com.rrsgroup.customer.domain.CrmCustomer
import com.rrsgroup.customer.domain.CrmPhoneNumber
import com.rrsgroup.customer.domain.CrmType
import com.rrsgroup.customer.domain.CustomerSearchRequest
import com.rrsgroup.customer.entity.CrmConfig
import com.rrsgroup.customer.entity.Customer
import spock.lang.Specification
import spock.lang.Subject

class CustomerCrmIntegrationServiceSpec extends Specification {

    def crmConfigService = Mock(CrmConfigService)
    def customerService = Mock(CustomerService)
    def crmService = Mock(CrmService)

    def crmServices = [:] as Map<String, CrmService>

    @Subject
    def service = new CustomerCrmIntegrationService(crmServices, crmConfigService, customerService)

    def "should search customers across CRM configs and return aggregated results"() {
        given: "a user and search request"
        def companyId = 100L
        def fieldUser = Mock(FieldUserDto) {
            getUserId() >> "user-1"
            getCompanyId() >> companyId
        }
        def request = new CustomerSearchRequest(firstName: "John")

        and: "company has two CRM configs"
        def crmConfig1 = new CrmConfig(id: 1L, crmType: CrmType.MOCK)
        def crmConfig2 = new CrmConfig(id: 2L, crmType: CrmType.WAITQUE)
        crmConfigService.getCrmConfigsForCompany(companyId) >> [crmConfig1, crmConfig2]

        and: "crmServices contains services for both"
        def crmService1 = Mock(CrmService)
        def crmService2 = Mock(CrmService)
        crmServices.put("mockCrmService", crmService1)
        crmServices.put("waitqueCrmService", crmService2)

        and: "CRM services return results"
        def crmAddress = new CrmAddress("123 Main St.", null, "Atlanta", "Georgia", "30303", "USA")
        def crmPhoneNumber = new CrmPhoneNumber(1, 1231231234)
        def crmCustomer1 = new CrmCustomer("crm-1", "John", "Doe", crmAddress, crmPhoneNumber)
        def crmCustomer2 = new CrmCustomer("crm-2", "Jane", "Smith", crmAddress, crmPhoneNumber)
        crmService1.searchCustomers(request) >> [crmCustomer1]
        crmService2.searchCustomers(request) >> [crmCustomer2]

        and: "customerService returns existing customers"
        def customer1 = new Customer(id: 10L, crmCustomerId: "crm-1")
        def customer2 = new Customer(id: 11L, crmCustomerId: "crm-2")
        customerService.getCustomerByCrmConfig(_ as CrmConfig, crmCustomer1) >> customer1
        customerService.getCustomerByCrmConfig(_ as CrmConfig, crmCustomer2) >> customer2

        when:
        def results = service.customerSearch(fieldUser, request)

        then:
        results.size() == 2
        results*.customer*.id == [10L, 11L]
        results*.crmCustomer*.crmCustomerId == ["crm-1", "crm-2"]
    }

    def "should create new customer if CRM customer not found in DB"() {
        given:
        def companyId = 200L
        def fieldUser = Mock(FieldUserDto) {
            getUserId() >> "user-1"
            getCompanyId() >> companyId
        }
        def request = new CustomerSearchRequest(crmCustomerId: "crm-xyz")

        def crmConfig = new CrmConfig(id: 5L, crmType: CrmType.MOCK)

        crmConfigService.getCrmConfigsForCompany(companyId) >> [crmConfig]
        crmServices.put("mockCrmService", crmService)

        def crmAddress = new CrmAddress("123 Main St.", null, "Atlanta", "Georgia", "30303", "USA")
        def crmPhoneNumber = new CrmPhoneNumber(1, 1231231234)
        def crmCustomer = new CrmCustomer("crm-xyz", "Alice", "Brown", crmAddress, crmPhoneNumber)
        crmService.getCustomerById("crm-xyz") >> Optional.of(crmCustomer)

        customerService.getCustomerByCrmConfig(crmConfig, crmCustomer) >> null
        def createdCustomer = new Customer(id: 99L, crmCustomerId: "crm-xyz")
        customerService.createCustomer(crmConfig, crmCustomer, fieldUser) >> createdCustomer

        when:
        def results = service.customerSearch(fieldUser, request)

        then:
        results.size() == 1
        results[0].customer.id == 99L
        results[0].crmCustomer.crmCustomerId == "crm-xyz"
    }

    def "should throw IllegalStateException if CRM service not configured"() {
        given:
        def crmConfig = new CrmConfig(crmType: CrmType.MOCK)
        def companyId = 10L
        def fieldUser = Mock(FieldUserDto) {
            getUserId() >> "user-1"
            getCompanyId() >> companyId
        }

        when:
        service.customerSearch(
                fieldUser,
                new CustomerSearchRequest()
        )

        then:
        1 * crmConfigService.getCrmConfigsForCompany(companyId) >> [crmConfig]
        thrown(IllegalStateException)
    }

    def "should handle case when CRM service returns empty customer list"() {
        given:
        def companyId = 300L
        def fieldUser = Mock(FieldUserDto) {
            getUserId() >> "user-1"
            getCompanyId() >> companyId
        }
        def request = new CustomerSearchRequest(firstName: "Ghost")

        def crmConfig = new CrmConfig(id: 9L, crmType: CrmType.MOCK)

        crmConfigService.getCrmConfigsForCompany(companyId) >> [crmConfig]
        crmServices.put("mockCrmService", crmService)
        crmService.searchCustomers(request) >> []

        when:
        def results = service.customerSearch(fieldUser, request)

        then:
        results.isEmpty()
    }
}
