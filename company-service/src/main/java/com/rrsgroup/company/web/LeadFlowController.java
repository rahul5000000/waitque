package com.rrsgroup.company.web;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.company.domain.Status;
import com.rrsgroup.company.dto.ActiveLeadFlowListDto;
import com.rrsgroup.company.dto.LeadFlowDto;
import com.rrsgroup.company.dto.LeadFlowListDto;
import com.rrsgroup.company.entity.LeadFlow;
import com.rrsgroup.company.service.ActiveLeadFlowDtoMapper;
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
    private final ActiveLeadFlowDtoMapper activeLeadFlowDtoMapper;

    @Autowired
    public LeadFlowController(
            LeadFlowDtoMapper leadFlowDtoMapper,
            LeadFlowService leadFlowService,
            ActiveLeadFlowDtoMapper activeLeadFlowDtoMapper) {
        this.leadFlowDtoMapper = leadFlowDtoMapper;
        this.leadFlowService = leadFlowService;
        this.activeLeadFlowDtoMapper = activeLeadFlowDtoMapper;
    }

    @PostMapping("/api/admin/flows")
    public LeadFlowDto addLeadFlow(@AuthenticationPrincipal AdminUserDto user, @RequestBody LeadFlowDto request) {
        Long companyId = user.getCompanyId();

        if(request.id() != null) {
            throw new IllegalRequestException("The 'id' field shall not be populated in a add lead flow request");
        }

        if(request.companyId() != null && !request.companyId().equals(companyId)) {
            throw new IllegalUpdateException("The companyId in the request body is not the user's company");
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

    @GetMapping("/api/admin/flows/{leadFlowId}")
    public LeadFlowDto getLeadFlow(@AuthenticationPrincipal AdminUserDto user, @PathVariable(name = "leadFlowId") Long leadFlowId) {
        Long companyId = user.getCompanyId();
        LeadFlow leadFlow = leadFlowService.getLeadFlow(leadFlowId, companyId);

        if(leadFlow == null) {
            throw new RecordNotFoundException("The leadFlowId=" + leadFlowId + " was not found");
        }

        return leadFlowDtoMapper.map(leadFlow);
    }

    @PostMapping("/api/admin/flows/{leadFlowId}")
    public LeadFlowDto updateLeadFlow(@AuthenticationPrincipal AdminUserDto user, @PathVariable(name = "leadFlowId") Long leadFlowId, @RequestBody LeadFlowDto request) {
        Long companyId = user.getCompanyId();

        if(request.id() != null &&  !request.id().equals(leadFlowId)) {
            throw new IllegalRequestException("The leadFlowId in the URL does not match the leadFlowId in the request body");
        }

        LeadFlow updateRequest = leadFlowDtoMapper.map(request);
        updateRequest.setId(leadFlowId);

        return leadFlowDtoMapper.map(leadFlowService.updateLeadFlow(updateRequest, companyId, user));
    }

    @DeleteMapping("/api/admin/flows/{leadFlowId}")
    public LeadFlowDto inactivateLeadFlow(
            @AuthenticationPrincipal AdminUserDto user,
            @PathVariable(name = "leadFlowId") Long leadFlowId) {
        Long companyId = user.getCompanyId();
        return leadFlowDtoMapper.map(leadFlowService.inactivateLeadFlow(leadFlowId, companyId, user));
    }

    @GetMapping("/api/public/companies/{companyId}/flows")
    public ActiveLeadFlowListDto publicGetListOfLeadFlows(
            @PathVariable(name = "companyId") Long companyId,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "sortField", required = false, defaultValue = "ordinal") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") SortDirection sortDir) {
        Page<LeadFlow> pageOfLeadFlows = leadFlowService.getCompanyListOfLeadFlows(companyId, List.of(Status.ACTIVE), limit, page, sortField, sortDir);
        return activeLeadFlowDtoMapper.map(pageOfLeadFlows);
    }
}
