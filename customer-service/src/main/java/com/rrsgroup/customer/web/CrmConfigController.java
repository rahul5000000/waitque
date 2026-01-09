package com.rrsgroup.customer.web;
import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.dto.SuperUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.customer.dto.CrmConfigDto;
import com.rrsgroup.customer.entity.CrmConfig;
import com.rrsgroup.customer.entity.nativecrm.NativeCrmConfig;
import com.rrsgroup.customer.service.CrmConfigDtoMapper;
import com.rrsgroup.customer.service.CrmConfigService;
import com.rrsgroup.customer.service.nativecrm.NativeCrmService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class CrmConfigController {
    private final CrmConfigService crmConfigService;
    private final CrmConfigDtoMapper crmConfigDtoMapper;
    private final NativeCrmService nativeCrmService;

    @Autowired
    public CrmConfigController(
            CrmConfigService crmConfigService,
            CrmConfigDtoMapper crmConfigDtoMapper,
            NativeCrmService nativeCrmService) {
        this.crmConfigService = crmConfigService;
        this.crmConfigDtoMapper = crmConfigDtoMapper;
        this.nativeCrmService = nativeCrmService;
    }

    @PostMapping("/api/internal/crm/configs")
    public CrmConfigDto superUserCreateCrmConfig(@AuthenticationPrincipal SuperUserDto user, @RequestBody CrmConfigDto request) {
        CrmConfig savedConfig = crmConfigService.addCrmConfig(crmConfigDtoMapper.map(request), user);
        provisionCrm(savedConfig);
        log.debug("Superuser created CRM config: {}", savedConfig);

        return crmConfigDtoMapper.map(savedConfig);
    }

    @PostMapping("/api/admin/crm/configs")
    public CrmConfigDto adminCreateCrmConfig(@AuthenticationPrincipal AdminUserDto user, @RequestBody CrmConfigDto request) {
        if(request.companyId() != null && !request.companyId().equals(user.getCompanyId())) {
            throw new IllegalUpdateException("The companyId does not match the user's companyId");
        }

        CrmConfig requestedConfig = crmConfigDtoMapper.map(request);
        requestedConfig.setCompanyId(user.getCompanyId());
        CrmConfig savedConfig = crmConfigService.addCrmConfig(requestedConfig, user);
        provisionCrm(savedConfig);
        log.debug("Admin created CRM config: {}", savedConfig);

        return crmConfigDtoMapper.map(savedConfig);
    }

    private void provisionCrm(CrmConfig crmConfig) {
        switch (crmConfig.getCrmType()) {
            case WAITQUE -> nativeCrmService.initNativeCrm(crmConfig);
            case MOCK -> doNothing();
            default -> throw new IllegalRequestException("CrmType=" + crmConfig.getCrmType() + " is not supported");
        }
    }

    private void doNothing() {

    }
}
