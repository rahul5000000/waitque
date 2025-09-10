package com.rrsgroup.company.web;

import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.company.dto.LeadFlowDto;
import com.rrsgroup.company.entity.LeadFlow;
import com.rrsgroup.company.service.LeadFlowDtoMapper;
import com.rrsgroup.company.service.LeadFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class LeadFlowController {
    private final LeadFlowDtoMapper leadFlowDtoMapper;
    private final LeadFlowService leadFlowService;

    @Autowired
    public LeadFlowController(LeadFlowDtoMapper leadFlowDtoMapper, LeadFlowService leadFlowService) {
        this.leadFlowDtoMapper = leadFlowDtoMapper;
        this.leadFlowService = leadFlowService;
    }

    @PostMapping("/api/admin/flows")
    public LeadFlowDto addLeadFlow(@AuthenticationPrincipal AdminUserDto user, @RequestBody LeadFlowDto request) {
        Long companyId = user.getCompanyId();

        if(request.id() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The 'id' field shall not be populated in a add lead flow request");
        }

        if(request.companyId() != null && !request.companyId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The companyId in the request body is not the user's company");
        }

        LeadFlow leadFlow = leadFlowDtoMapper.map(request);
        return leadFlowDtoMapper.map(leadFlowService.createLeadFlow(leadFlow, companyId, user));
    }
}
