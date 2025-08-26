package com.rrsgroup.waitque.dto;

import com.rrsgroup.waitque.domain.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedDto {
    private Integer page;
    private Integer limit;
    private Long total;
    private String sortField;
    private SortDirection sortDir;
}
