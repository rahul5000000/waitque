package com.rrsgroup.customer.service;

import com.rrsgroup.customer.dto.AdminDashboardMetricsDto;
import com.rrsgroup.customer.entity.lead.LeadStatusCount;
import org.springframework.stereotype.Service;

@Service
public class DashboardDtoMapper {
    public AdminDashboardMetricsDto map(LeadStatusCount leadStatusCount, long unreadMessages) {
        return new AdminDashboardMetricsDto(
                leadStatusCount.getNewCount(),
                leadStatusCount.getNewCount() + leadStatusCount.getInProgressCount(),
                unreadMessages);
    }
}
