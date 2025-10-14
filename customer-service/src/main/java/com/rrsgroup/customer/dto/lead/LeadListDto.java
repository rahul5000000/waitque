package com.rrsgroup.customer.dto.lead;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.PaginatedDto;
import com.rrsgroup.customer.domain.lead.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LeadListDto extends PaginatedDto {
    private List<LeadListItem> leads;

    public LeadListDto(Integer page, Integer limit, Long total, String sortField, SortDirection sortDir, List<LeadListDto.LeadListItem> leads) {
        super(page, limit, total, sortField, sortDir);
        this.leads = leads;
    }

    @Data
    @AllArgsConstructor
    public static class LeadListItem {
        private Long id;
        private String firstName;
        private String lastName;
        private String leadFlowName;
        private String phoneNumber;
        private String email;
        private LeadStatus status;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;
    }
}
