package com.rrsgroup.common.util;

import com.rrsgroup.common.domain.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@AllArgsConstructor
public class PageableWrapper {
    private Pageable pageable;

    public String getSortField() {
        String sortField = pageable.getSort().stream().findFirst()
                .map(Sort.Order::getProperty).orElse("");
        return getLastPart(sortField);
    }

    public SortDirection getSortDir() {
        return pageable.getSort().stream().findFirst()
                .map(sort -> sort.getDirection() == Sort.Direction.ASC ? SortDirection.ASC : SortDirection.DESC).orElse(SortDirection.ASC);
    }

    public int getPageNumber() {
        return pageable.getPageNumber();
    }

    public int getPageSize() {
        return pageable.getPageSize();
    }

    private String getLastPart(String input) {
        if (input == null || input.isEmpty()) {
            return input; // return as-is for null/empty
        }
        int lastDot = input.lastIndexOf('.');
        if (lastDot == -1) {
            return input; // no dot, return whole string
        }
        return input.substring(lastDot + 1);
    }
}
