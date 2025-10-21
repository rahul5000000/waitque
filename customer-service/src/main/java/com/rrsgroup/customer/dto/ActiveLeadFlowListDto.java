package com.rrsgroup.customer.dto;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.PaginatedDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ActiveLeadFlowListDto extends PaginatedDto {
    private List<ActiveLeadFlowListDto.LeadFlowListItem> leadFlows;

    public ActiveLeadFlowListDto(Integer page, Integer limit, Long total, String sortField, SortDirection sortDir, List<ActiveLeadFlowListDto.LeadFlowListItem> leadFlows) {
        super(page, limit, total, sortField, sortDir);
        this.leadFlows = leadFlows;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LeadFlowListItem {
        private Long id;
        private String name;
        private Integer ordinal;
        private String icon;
    }
}