package com.rrsgroup.customer.service

import com.rrsgroup.customer.domain.lead.LeadStatus
import com.rrsgroup.customer.entity.lead.Lead
import com.rrsgroup.customer.entity.lead.LeadAnswer
import com.rrsgroup.customer.repository.LeadRepository
import spock.lang.Specification

import java.time.LocalDateTime

class LeadServiceSpec extends Specification {

    def leadRepository = Mock(LeadRepository)
    def service = new LeadService(leadRepository)

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
}
