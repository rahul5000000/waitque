package com.rrsgroup.company.dto;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.PaginatedDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class ActiveLeadFlowListDto extends PaginatedDto {
    private List<ActiveLeadFlowListDto.LeadFlowListItem> leadFlows;

    public ActiveLeadFlowListDto(Integer page, Integer limit, Long total, String sortField, SortDirection sortDir, List<ActiveLeadFlowListDto.LeadFlowListItem> leadFlows) {
        super(page, limit, total, sortField, sortDir);
        this.leadFlows = leadFlows;
    }

    @Data
    @AllArgsConstructor
    public static class LeadFlowListItem {
        private Long id;
        private String name;
        private Integer ordinal;
        private String icon;
    }
}
