package com.rrsgroup.customer.service.lead

import com.rrsgroup.common.dto.AddressDto
import com.rrsgroup.common.dto.PhoneNumberDto
import com.rrsgroup.common.entity.Address
import com.rrsgroup.common.entity.PhoneNumber
import com.rrsgroup.common.service.CommonDtoMapper
import com.rrsgroup.customer.domain.CrmAddress
import com.rrsgroup.customer.domain.CrmCustomer
import com.rrsgroup.customer.domain.CrmPhoneNumber
import com.rrsgroup.customer.domain.LeadFlowQuestionDataType
import com.rrsgroup.customer.domain.LeadFlowStatus
import com.rrsgroup.customer.domain.lead.LeadStatus
import com.rrsgroup.customer.dto.LeadFlowDto
import com.rrsgroup.customer.dto.LeadFlowQuestionDto
import com.rrsgroup.customer.dto.lead.LeadAnswerDto
import com.rrsgroup.customer.dto.lead.LeadBooleanAnswerDto
import com.rrsgroup.customer.dto.lead.LeadDto
import com.rrsgroup.customer.dto.lead.LeadNumberAnswerDto
import com.rrsgroup.customer.dto.lead.LeadTextAnswerDto
import com.rrsgroup.customer.entity.CrmConfig
import com.rrsgroup.customer.entity.Customer
import com.rrsgroup.customer.entity.lead.Lead
import com.rrsgroup.customer.entity.lead.LeadAnswer
import com.rrsgroup.customer.service.CustomerCrmIntegrationService
import com.rrsgroup.customer.service.LeadFlowService
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class LeadDtoMapperSpec extends Specification {

    def commonDtoMapper = Mock(CommonDtoMapper)
    def customerCrmIntegrationService = Mock(CustomerCrmIntegrationService)
    def leadFlowService = Mock(LeadFlowService)

    def dtoMapper = new LeadDtoMapper(commonDtoMapper, customerCrmIntegrationService, leadFlowService)

    def "map should map all simple fields and call map for address and phone when present"() {
        given:
        def customer = new Customer(id: 100L)
        def id = 123L
        def address1 = "address1"
        def address2 = "address2"
        def city = "city"
        def state = "state"
        def zipcode = "zipcode"
        def country = "country"
        def addressDto = new AddressDto(id, address1, address2, city, state, zipcode, country)
        def countryCode = 1
        def phoneNumber = 1234567
        def phoneDto = new PhoneNumberDto(id, countryCode, phoneNumber)

        def dto = new LeadDto(id, 55L, LeadStatus.NEW, "Alice", "Smith", addressDto, phoneDto, "alice@test.com", [
                Mock(LeadAnswerDto) {
                    getLeadFlowQuestionId() >> 1L
                    getDataType() >> LeadFlowQuestionDataType.TEXT
                },
                Mock(LeadAnswerDto) {
                    getLeadFlowQuestionId() >> 2L
                    getDataType() >> LeadFlowQuestionDataType.BOOLEAN
                }
        ], null, null, null, null, null, null, null)

        and:
        def mappedAddress = new Address(id, address1, address2, city, state, zipcode, country)
        def mappedPhone = new PhoneNumber(id, countryCode, phoneNumber)

        // Spy on dtoMapper.map(LeadAnswerDto, Lead)
        dtoMapper = Spy(LeadDtoMapper, constructorArgs: [commonDtoMapper, customerCrmIntegrationService, leadFlowService])
        dtoMapper.map(_ as LeadAnswerDto, _ as Lead) >> { LeadAnswerDto a, Lead l -> new LeadAnswer(leadFlowQuestionId: a.getLeadFlowQuestionId(), dataType: a.getDataType()) }

        when:
        def result = dtoMapper.map(dto, customer)

        then:
        result.leadFlowId == 55L
        result.status.name() == "NEW"
        result.overrideFirstName == "Alice"
        result.overrideLastName == "Smith"
        result.overrideEmail == "alice@test.com"
        result.overrideAddress == mappedAddress
        result.overridePhoneNumber == mappedPhone
        result.customer == customer

        // Verify that answers were mapped via the sub-mapper
        result.answers.size() == 2
        result.answers*.leadFlowQuestionId.containsAll([1L, 2L])
        result.answers*.dataType*.name().containsAll(["TEXT", "BOOLEAN"])

        // Verify mocks called
        1 * commonDtoMapper.map(addressDto) >> mappedAddress
        1 * commonDtoMapper.map(phoneDto) >> mappedPhone
    }

    def "map should skip address and phone mapping when null"() {
        given:
        def customer = new Customer(id: 200L)
        def dto = new LeadDto(1L, 99L, LeadStatus.IN_PROGRESS, "Johnson", "Smith", null, null, "bob@example.com", [], null, null, null, null, null, null, null)

        when:
        def result = dtoMapper.map(dto, customer)

        then:
        result.overrideAddress == null
        result.overridePhoneNumber == null
        result.customer == customer
        0 * commonDtoMapper.map(_)
    }

    def "map(LeadAnswerDto, Lead) maps BOOLEAN type correctly"() {
        given:
        def lead = Lead.builder().id(1L).build()
        def dto = new LeadBooleanAnswerDto(1L, 100L, true)

        when:
        def result = dtoMapper.map(dto, lead)

        then:
        result.lead == lead
        result.leadFlowQuestionId == 100L
        result.booleanAnswer
        result.dataType == LeadFlowQuestionDataType.BOOLEAN
    }

    def "map(LeadAnswerDto, Lead) maps TEXT type correctly"() {
        given:
        def lead = Lead.builder().id(1L).build()
        def dto = new LeadTextAnswerDto(2L, 200L, "hello")

        when:
        def result = dtoMapper.map(dto, lead)

        then:
        result.lead == lead
        result.textAnswer == "hello"
        result.dataType == LeadFlowQuestionDataType.TEXT
    }

    def "map(LeadAnswerDto, Lead) maps NUMBER type correctly"() {
        given:
        def lead = Lead.builder().id(1L).build()
        def dto = new LeadNumberAnswerDto(3L, 300L, 42)

        when:
        def result = dtoMapper.map(dto, lead)

        then:
        result.numberAnswer == 42
        result.dataType == LeadFlowQuestionDataType.NUMBER
    }

    def "map(LeadAnswer) maps BOOLEAN LeadAnswer to LeadBooleanAnswerDto"() {
        given:
        def answer = LeadAnswer.builder()
                .id(10L)
                .leadFlowQuestionId(200L)
                .dataType(LeadFlowQuestionDataType.BOOLEAN)
                .booleanAnswer(true)
                .build()

        when:
        def dto = dtoMapper.map(answer)

        then:
        dto instanceof LeadBooleanAnswerDto
        ((LeadBooleanAnswerDto)dto).getEnabled()
        dto.getLeadFlowQuestionId() == 200L
    }

    def "map(LeadAnswer) maps TEXT LeadAnswer to LeadTextAnswerDto"() {
        given:
        def answer = LeadAnswer.builder()
                .id(20L)
                .leadFlowQuestionId(201L)
                .dataType(LeadFlowQuestionDataType.TEXT)
                .textAnswer("hi")
                .build()

        when:
        def dto = dtoMapper.map(answer)

        then:
        dto instanceof LeadTextAnswerDto
        ((LeadTextAnswerDto)dto).getText() == "hi"
    }

    def "map(Lead) maps Lead with CRM and LeadFlow info"() {
        given:
        def crmConfig = new CrmConfig(id: 1L, companyId: 2L)
        def customer = new Customer(id: 10L, crmCustomerId: "C123", crmConfig: crmConfig)
        def lead = Lead.builder()
                .id(5L)
                .leadFlowId(9L)
                .customer(customer)
                .status(LeadStatus.NEW)
                .overrideEmail("x@test.com")
                .answers([])
                .build()

        def crmAddress = new CrmAddress("123 Main St.", null, "Atlanta", "Georgia", "30303", "USA")
        def crmPhoneNumber = new CrmPhoneNumber(1, 1231231234)
        def crmCustomer = new CrmCustomer("crm-123", "John", "Doe", crmAddress, crmPhoneNumber, "john.d@test.com")
        def leadFlow = generateLeadFlow(LeadFlowStatus.ACTIVE)

        customerCrmIntegrationService.getCrmCustomer(_, _) >> Optional.of(crmCustomer)
        leadFlowService.getLeadFlow(_, _) >> Optional.of(leadFlow)

        when:
        def dto = dtoMapper.map(lead)

        then:
        dto.id == 5L
        dto.leadFlowId == 9L
        dto.overrideEmail == "x@test.com"
        dto.crmCustomer == crmCustomer
        dto.leadFlow == leadFlow
    }

    def "map(Page<Lead>) builds LeadListDto with mapped items"() {
        given:
        def crmConfig = new CrmConfig(id: 1L, companyId: 2L)
        def customer = new Customer(id: 1L, crmCustomerId: "CRM1", crmConfig: crmConfig)
        def lead = Lead.builder()
                .id(11L)
                .customer(customer)
                .leadFlowId(22L)
                .status(LeadStatus.NEW)
                .overrideFirstName("Alice")
                .overrideLastName("Smith")
                .overrideEmail("alice@test.com")
                .overridePhoneNumber(new PhoneNumber(countryCode: 1, phoneNumber: 9999999999L))
                .build()

        def crmAddress = new CrmAddress("123 Main St.", null, "Atlanta", "Georgia", "30303", "USA")
        def crmPhoneNumber = new CrmPhoneNumber(1, 1231231234)
        def crmCustomer = new CrmCustomer("crm-123", "John", "Doe", crmAddress, crmPhoneNumber, "john.d@test.com")
        def leadFlow = generateLeadFlow(LeadFlowStatus.ACTIVE)
        def page = new PageImpl<>([lead], PageRequest.of(0, 10), 1)

        customerCrmIntegrationService.getCrmCustomer(_, _) >> Optional.of(crmCustomer)
        leadFlowService.getLeadFlow(_, _) >> Optional.of(leadFlow)

        when:
        def result = dtoMapper.map(page)

        then:
        result.page == 0
        result.limit == 10
        result.total == 1
        result.leads.size() == 1
        result.leads[0].firstName == "Alice"
        result.leads[0].lastName == "Smith"
        result.leads[0].leadFlowName == "FlowName"
        result.leads[0].email == "alice@test.com"
    }

    def "map(Page<Lead>) builds LeadListDto with mapped items and uses names from CRM is override not provided"() {
        given:
        def crmConfig = new CrmConfig(id: 1L, companyId: 2L)
        def customer = new Customer(id: 1L, crmCustomerId: "CRM1", crmConfig: crmConfig)
        def lead = Lead.builder()
                .id(11L)
                .customer(customer)
                .leadFlowId(22L)
                .status(LeadStatus.NEW)
                .build()

        def crmAddress = new CrmAddress("123 Main St.", null, "Atlanta", "Georgia", "30303", "USA")
        def crmPhoneNumber = new CrmPhoneNumber(1, 1231231234)
        def crmCustomer = new CrmCustomer("crm-123", "John", "Doe", crmAddress, crmPhoneNumber, "john.d@test.com")
        def leadFlow = generateLeadFlow(LeadFlowStatus.ACTIVE)
        def page = new PageImpl<>([lead], PageRequest.of(0, 10), 1)

        customerCrmIntegrationService.getCrmCustomer(_, _) >> Optional.of(crmCustomer)
        leadFlowService.getLeadFlow(_, _) >> Optional.of(leadFlow)

        when:
        def result = dtoMapper.map(page)

        then:
        result.page == 0
        result.limit == 10
        result.total == 1
        result.leads.size() == 1
        result.leads[0].firstName == "John"
        result.leads[0].lastName == "Doe"
        result.leads[0].leadFlowName == "FlowName"
        result.leads[0].email == "john.d@test.com"
    }

    private LeadFlowDto generateLeadFlow(LeadFlowStatus status) {
        def id = 1L
        def companyId = 2L
        def name = "FlowName"
        def iconUrl = "test.jpg"
        def buttonText = "Schedule"
        def title = "Book Test"
        def confirmationMessageHeader = "confirmationMessageHeader"
        def confirmationMessage1 = "confirmationMessage1"
        def confirmationMessage2 = "confirmationMessage2"
        def confirmationMessage3 = "confirmationMessage3"
        def ordinal = 0
        def question1Id = 1L
        def question1 = "question1"
        def question1DataType = LeadFlowQuestionDataType.BOOLEAN
        def question1IsRequired = true
        def question2Id = 2L
        def question2 = "question2"
        def question2DataType = LeadFlowQuestionDataType.TEXT
        def question2IsRequired = false

        List<LeadFlowQuestionDto> questionDtos = new ArrayList<>()
        questionDtos.add(new LeadFlowQuestionDto(question1Id, question1, question1DataType, question1IsRequired))
        questionDtos.add(new LeadFlowQuestionDto(question2Id, question2, question2DataType, question2IsRequired))

        return new LeadFlowDto(id, companyId, status, name, iconUrl, buttonText, title, confirmationMessageHeader,
                confirmationMessage1, confirmationMessage2, confirmationMessage3, ordinal, questionDtos, null)
    }
}
