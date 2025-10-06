package com.rrsgroup.customer.service

import com.rrsgroup.common.dto.UserDto
import com.rrsgroup.common.exception.RecordNotFoundException
import com.rrsgroup.customer.entity.CrmConfig
import com.rrsgroup.customer.repository.CrmConfigRepository
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Specification

class CrmConfigServiceSpec extends Specification {

    def crmConfigRepository = Mock(CrmConfigRepository)
    def service = new CrmConfigService(crmConfigRepository)

    def "should save CrmConfig with audit fields populated"() {
        given:
        def user = Mock(UserDto) {
            getUserId() >> "user-123"
        }
        def config = new CrmConfig()
        config.companyId = 10L
        def savedConfig = new CrmConfig(id: 1L, companyId: 10L)

        when:
        def result = service.addCrmConfig(config, user)

        then:
        result.id == 1L
        result.companyId == 10L
        1 * crmConfigRepository.save(_ as CrmConfig) >> { CrmConfig arg ->
            assert arg.createdBy == "user-123"
            assert arg.updatedBy == "user-123"
            assert arg.createdDate != null
            assert arg.updatedDate != null
            return savedConfig
        }
    }

    def "should throw RecordNotFoundException when DataIntegrityViolationException contains fk_crm_config_company"() {
        given:
        def user = Mock(UserDto) {
            getUserId() >> "user-123"
        }
        def config = new CrmConfig(companyId: 99L)
        crmConfigRepository.save(_ as CrmConfig) >> {
            throw new DataIntegrityViolationException("violates foreign key constraint fk_crm_config_company")
        }

        when:
        service.addCrmConfig(config, user)

        then:
        def ex = thrown(RecordNotFoundException)
        ex.message == "Company not found with companyId=99"
    }

    def "should rethrow DataIntegrityViolationException for unrelated constraint"() {
        given:
        def user = Mock(UserDto) {
            getUserId() >> "user-123"
        }
        def config = new CrmConfig(companyId: 5L)
        crmConfigRepository.save(_ as CrmConfig) >> {
            throw new DataIntegrityViolationException("unique constraint crm_name")
        }

        when:
        service.addCrmConfig(config, user)

        then:
        thrown(DataIntegrityViolationException)
    }

    def "should return CrmConfigs for given companyId"() {
        given:
        def companyId = 42L
        def configs = [
                new CrmConfig(id: 1L, companyId: companyId),
                new CrmConfig(id: 2L, companyId: companyId)
        ]

        when:
        def result = service.getCrmConfigsForCompany(companyId)

        then:
        result.size() == 2
        result*.companyId.every { it == companyId }
        1 * crmConfigRepository.findByCompanyId(companyId) >> configs
    }
}
