package com.rrsgroup.customer.web;

import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.customer.dto.AdminDashboardMetricsDto;
import com.rrsgroup.customer.entity.lead.LeadStatusCount;
import com.rrsgroup.customer.service.DashboardDtoMapper;
import com.rrsgroup.customer.service.lead.LeadService;
import com.rrsgroup.customer.service.message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {
    private final LeadService leadService;
    private final DashboardDtoMapper dashboardDtoMapper;
    private final MessageService messageService;

    @Autowired
    public DashboardController(LeadService leadService, DashboardDtoMapper dashboardDtoMapper, MessageService messageService) {
        this.messageService = messageService;
        this.leadService = leadService;
        this.dashboardDtoMapper = dashboardDtoMapper;
    }

    @GetMapping("/api/admin/dashboard/metrics")
    public AdminDashboardMetricsDto getAdminDashboardMetrics(@AuthenticationPrincipal AdminUserDto user) {
        Long companyId = user.getCompanyId();

        LeadStatusCount leadStatusCount = leadService.getLeadStatusCountForCompany(companyId);
        long unreadMessages = messageService.countUnreadMessages(companyId);

        return dashboardDtoMapper.map(leadStatusCount, unreadMessages);
    }
}
