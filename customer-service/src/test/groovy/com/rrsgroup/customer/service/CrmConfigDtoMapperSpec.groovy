package com.rrsgroup.customer.service

import com.rrsgroup.customer.domain.CrmType
import com.rrsgroup.customer.dto.CrmConfigDto
import com.rrsgroup.customer.entity.CrmConfig
import spock.lang.Specification

class CrmConfigDtoMapperSpec extends Specification {

    def mapper = new CrmConfigDtoMapper()

    def "should map CrmConfigDto to CrmConfig entity"() {
        given:
        def dto = new CrmConfigDto(
                1L,
                100L,
                CrmType.MOCK,
                "Mock CRM"
        )

        when:
        def entity = mapper.map(dto)

        then:
        entity.id == dto.id()
        entity.companyId == dto.companyId()
        entity.crmType == dto.crmType()
        entity.crmName == dto.crmName()
    }

    def "should map CrmConfig entity to CrmConfigDto"() {
        given:
        def entity = CrmConfig.builder()
                .id(2L)
                .companyId(200L)
                .crmType(CrmType.MOCK)
                .crmName("Mock CRM")
                .build()

        when:
        def dto = mapper.map(entity)

        then:
        dto.id() == entity.id
        dto.companyId() == entity.companyId
        dto.crmType() == entity.crmType
        dto.crmName() == entity.crmName
    }

    def "should handle null fields gracefully when mapping CrmConfigDto to entity"() {
        given:
        def dto = new CrmConfigDto(null, null, null, null)

        when:
        def entity = mapper.map(dto)

        then:
        entity.id == null
        entity.companyId == null
        entity.crmType == null
        entity.crmName == null
    }

    def "should handle null fields gracefully when mapping CrmConfig entity to dto"() {
        given:
        def entity = CrmConfig.builder().build()

        when:
        def dto = mapper.map(entity)

        then:
        dto.id() == null
        dto.companyId() == null
        dto.crmType() == null
        dto.crmName() == null
    }
}
