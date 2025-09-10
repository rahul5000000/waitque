package com.rrsgroup.company.web;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.company.domain.Status;
import com.rrsgroup.company.dto.LeadFlowDto;
import com.rrsgroup.company.dto.LeadFlowListDto;
import com.rrsgroup.company.entity.LeadFlow;
import com.rrsgroup.company.service.LeadFlowDtoMapper;
import com.rrsgroup.company.service.LeadFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

    @GetMapping("/api/admin/flows")
    public LeadFlowListDto getListOfLeadFlows(
            @AuthenticationPrincipal AdminUserDto user,
            @RequestParam(name = "status", required = false) List<Status> statuses,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "sortField", required = false, defaultValue = "ordinal") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") SortDirection sortDir) {
        Long companyId = user.getCompanyId();
        Page<LeadFlow> pageOfLeadFlows = leadFlowService.getCompanyListOfLeadFlows(companyId, statuses, limit, page, sortField, sortDir);

        return leadFlowDtoMapper.map(pageOfLeadFlows);
    }
}
