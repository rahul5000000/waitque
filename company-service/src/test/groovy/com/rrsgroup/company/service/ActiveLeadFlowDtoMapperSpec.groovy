package com.rrsgroup.company.service

import com.rrsgroup.company.domain.LeadFlowStatus
import com.rrsgroup.company.dto.ActiveLeadFlowListDto
import com.rrsgroup.company.entity.LeadFlow
import com.rrsgroup.company.entity.LeadFlowOrder
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import spock.lang.Specification

class ActiveLeadFlowDtoMapperSpec extends Specification {

    def mapper = new ActiveLeadFlowDtoMapper()

    def "map should convert Page<LeadFlow> into ActiveLeadFlowListDto"() {
        given:
        def leadFlow1 = new LeadFlow(id: 1L, name: "Lead Flow A",
                leadFlowOrder: new LeadFlowOrder(ordinal: 1, status: LeadFlowStatus.ACTIVE),
                icon: "icon-a.png")
        def leadFlow2 = new LeadFlow(id: 2L, name: "Lead Flow B",
                leadFlowOrder: new LeadFlowOrder(ordinal: 2, status: LeadFlowStatus.ACTIVE),
                icon: "icon-b.png")

        def pageable = PageRequest.of(0, 2, Sort.by(Sort.Order.asc("leadFlowOrder.ordinal")))
        Page<LeadFlow> page = new PageImpl([leadFlow1, leadFlow2], pageable, 2)

        when:
        ActiveLeadFlowListDto dto = mapper.map(page)

        then:
        dto.getPage() == 0
        dto.getLimit() == 2
        dto.getTotal() == 2
        dto.sortField == "ordinal"   // last part of "leadFlowOrder.ordinal"
        dto.sortDir.name() == "ASC"

        dto.getLeadFlows().size() == 2
        dto.getLeadFlows()[0].id == 1L
        dto.getLeadFlows()[0].name == "Lead Flow A"
        dto.getLeadFlows()[0].ordinal == 1
        dto.getLeadFlows()[0].icon == "icon-a.png"

        dto.getLeadFlows()[1].id == 2L
        dto.getLeadFlows()[1].name == "Lead Flow B"
        dto.getLeadFlows()[1].ordinal == 2
        dto.getLeadFlows()[1].icon == "icon-b.png"
    }

    def "map should handle empty page"() {
        given:
        def pageable = PageRequest.of(1, 5, Sort.by(Sort.Order.desc("name")))
        Page<LeadFlow> page = new PageImpl([], pageable, 0)

        when:
        def dto = mapper.map(page)

        then:
        dto.getPage() == 1
        dto.getLimit() == 5
        dto.getTotal() == 0
        dto.getLeadFlows().isEmpty()
        dto.sortField == "name"
        dto.sortDir.name() == "DESC"
    }
}
