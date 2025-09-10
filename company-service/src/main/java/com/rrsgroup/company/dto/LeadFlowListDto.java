package com.rrsgroup.company.dto;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.PaginatedDto;
import com.rrsgroup.company.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class LeadFlowListDto extends PaginatedDto {
    private List<LeadFlowListItem> leadFlows;

    public LeadFlowListDto(Integer page, Integer limit, Long total, String sortField, SortDirection sortDir, List<LeadFlowListDto.LeadFlowListItem> leadFlows) {
        super(page, limit, total, sortField, sortDir);
        this.leadFlows = leadFlows;
    }

    @Data
    @AllArgsConstructor
    public static class LeadFlowListItem {
        private Long id;
        private String name;
        private Status status;
        private Integer ordinal;
    }
}
