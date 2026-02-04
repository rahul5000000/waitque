package com.rrsgroup.customer.service;

import com.rrsgroup.customer.dto.AdminDashboardMetricsDto;
import com.rrsgroup.customer.entity.lead.LeadStatusCount;
import org.springframework.stereotype.Service;

@Service
public class DashboardDtoMapper {
    public AdminDashboardMetricsDto map(LeadStatusCount leadStatusCount, long unreadMessages) {
        long newCount = leadStatusCount.getNewCount() == null ? 0 : leadStatusCount.getNewCount();
        long inProgressCount = leadStatusCount.getInProgressCount() == null ? 0 : leadStatusCount.getInProgressCount();
        return new AdminDashboardMetricsDto(
                newCount,
                newCount + inProgressCount,
                unreadMessages);
    }
}
