package com.rrsgroup.waitque.dto;

import com.rrsgroup.waitque.domain.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class CompanyListDto extends PaginatedDto {
    private List<CompanyListItem> companies;

    public CompanyListDto(Integer page, Integer limit, Long total, String sortField, SortDirection sortDir, List<CompanyListItem> companies) {
        super(page, limit, total, sortField, sortDir);
        this.companies = companies;
    }

    @Data
    @AllArgsConstructor
    public static class CompanyListItem {
        private Long id;
        private String name;
        private String logoUrl;
    }
}
