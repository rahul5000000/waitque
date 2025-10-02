package com.rrsgroup.company.service;

import com.rrsgroup.common.util.PageableWrapper;
import com.rrsgroup.company.dto.ActiveLeadFlowListDto;
import com.rrsgroup.company.entity.LeadFlow;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ActiveLeadFlowDtoMapper {
    public ActiveLeadFlowListDto map(Page<LeadFlow> pageOfLeadFlows) {
        PageableWrapper pageable = new PageableWrapper(pageOfLeadFlows.getPageable());
        return new ActiveLeadFlowListDto(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageOfLeadFlows.getTotalElements(),
                pageable.getSortField(),
                pageable.getSortDir(),
                pageOfLeadFlows.getContent().stream().map(leadFlow -> new ActiveLeadFlowListDto.LeadFlowListItem(leadFlow.getId(), leadFlow.getName(), leadFlow.getLeadFlowOrder().getOrdinal(), leadFlow.getIcon())).toList()
        );
    }
}
