package com.rrsgroup.customer.service.lead

import com.rrsgroup.common.domain.SortDirection
import com.rrsgroup.common.dto.CompanyUserDto
import com.rrsgroup.customer.domain.lead.LeadStatus
import com.rrsgroup.customer.entity.lead.Lead
import com.rrsgroup.customer.entity.lead.LeadAnswer
import com.rrsgroup.customer.repository.LeadRepository
import com.rrsgroup.customer.service.EventService
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import spock.lang.Specification

class LeadServiceSpec extends Specification {

    def leadRepository = Mock(LeadRepository)
    def eventService = Mock(EventService)
    def service = new LeadService(leadRepository, eventService)

    def "createLeadAnonymous should set metadata and save lead"() {
        given:
        def answer1 = new LeadAnswer()
        def answer2 = new LeadAnswer()
        def lead = new Lead()
        lead.answers = [answer1, answer2]

        when:
        def result = service.createLeadAnonymous(lead)

        then:
        1 * leadRepository.save(_ as Lead) >> { Lead l -> l } // echo back
        lead.status == LeadStatus.NEW

        // Verify created/updated fields set
        lead.createdBy == "anonymous"
        lead.updatedBy == "anonymous"
        lead.createdDate != null
        lead.updatedDate != null

        // Verify same timestamps for both answers
        lead.answers.each {
            assert it.createdBy == "anonymous"
            assert it.updatedBy == "anonymous"
            assert it.createdDate != null
            assert it.updatedDate != null
        }

        result == lead
    }

    def "createLeadAnonymous should handle empty answers list"() {
        given:
        def lead = new Lead()
        lead.answers = []

        when:
        def result = service.createLeadAnonymous(lead)

        then:
        1 * leadRepository.save(_ as Lead) >> { Lead l -> l }

        lead.status == LeadStatus.NEW
        lead.createdBy == "anonymous"
        lead.updatedBy == "anonymous"
        lead.createdDate != null
        lead.updatedDate != null
        result == lead
    }

    def "getCompanyListOfLeads should call findByCompanyId when statuses are null or empty"() {
        given:
        def companyId = 42L
        def pageableCapture = null
        def leads = [new Lead(id: 1L), new Lead(id: 2L)]
        def pageResult = new PageImpl<>(leads)

        when:
        def result = service.getCompanyListOfLeads(companyId, statuses, 10, 0, "createdDate", SortDirection.ASC)

        then:
        1 * leadRepository.findByCompanyId(companyId, _ as Pageable) >> { args ->
            pageableCapture = args[1]
            pageResult
        }

        result == pageResult
        pageableCapture.pageNumber == 0
        pageableCapture.pageSize == 10
        pageableCapture.sort.iterator().next().property == "createdDate"

        where:
        statuses << [null, []]  // test both null and empty cases
    }

    def "getCompanyListOfLeads should call findByCompanyIdAndStatusIn when statuses are provided"() {
        given:
        def companyId = 42L
        def statuses = [LeadStatus.NEW, LeadStatus.IN_PROGRESS]
        def pageableCapture = null
        def leads = [new Lead(id: 3L)]
        def pageResult = new PageImpl<>(leads)

        when:
        def result = service.getCompanyListOfLeads(companyId, statuses, 5, 2, "updatedDate", SortDirection.DESC)

        then:
        1 * leadRepository.findByCompanyIdAndStatusIn(companyId, statuses, _ as Pageable) >> { args ->
            pageableCapture = args[2]
            pageResult
        }

        result == pageResult
        pageableCapture.pageNumber == 2
        pageableCapture.pageSize == 5
        pageableCapture.sort.iterator().next().property == "updatedDate"
        pageableCapture.sort.iterator().next().direction.isDescending()
    }

    def "getLeadById should call repository with correct params"() {
        given:
        def lead = new Lead(id: 99L)
        def userDto = Mock(CompanyUserDto) {
            getCompanyId() >> 123L
        }

        when:
        def result = service.getLeadById(99L, userDto)

        then:
        1 * leadRepository.findByIdAndCustomer_CrmConfig_CompanyId(99L, 123L) >> Optional.of(lead)
        result.get() == lead
    }

    def "getLeadById should return empty optional when repository returns empty"() {
        given:
        def userDto = Mock(CompanyUserDto) {
            getCompanyId() >> 555L
        }

        when:
        def result = service.getLeadById(321L, userDto)

        then:
        1 * leadRepository.findByIdAndCustomer_CrmConfig_CompanyId(321L, 555L) >> Optional.empty()
        result.isEmpty()
    }
}
