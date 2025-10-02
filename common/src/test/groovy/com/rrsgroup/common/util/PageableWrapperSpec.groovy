package com.rrsgroup.common.util

import com.rrsgroup.common.domain.SortDirection
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import spock.lang.Specification

class PageableWrapperSpec extends Specification {

    def "getSortField should return last part of property name"() {
        given:
        Pageable pageable = PageRequest.of(0, 10, Sort.by("leadFlowOrder.ordinal"))
        def wrapper = new PageableWrapper(pageable)

        when:
        def field = wrapper.getSortField()

        then:
        field == "ordinal"
    }

    def "getSortField should return whole string if no dot"() {
        given:
        Pageable pageable = PageRequest.of(0, 10, Sort.by("status"))
        def wrapper = new PageableWrapper(pageable)

        expect:
        wrapper.getSortField() == "status"
    }

    def "getSortField should return empty string if no sort is defined"() {
        given:
        Pageable pageable = PageRequest.of(0, 10) // no sort
        def wrapper = new PageableWrapper(pageable)

        expect:
        wrapper.getSortField() == ""
    }

    def "getSortDir should return ASC when sort direction is ascending"() {
        given:
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name")))
        def wrapper = new PageableWrapper(pageable)

        expect:
        wrapper.getSortDir() == SortDirection.ASC
    }

    def "getSortDir should return DESC when sort direction is descending"() {
        given:
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("name")))
        def wrapper = new PageableWrapper(pageable)

        expect:
        wrapper.getSortDir() == SortDirection.DESC
    }

    def "getSortDir should default to ASC when no sort defined"() {
        given:
        Pageable pageable = PageRequest.of(0, 10) // no sort
        def wrapper = new PageableWrapper(pageable)

        expect:
        wrapper.getSortDir() == SortDirection.ASC
    }

    def "getPageNumber should return correct page index"() {
        given:
        Pageable pageable = PageRequest.of(5, 20) // page 5
        def wrapper = new PageableWrapper(pageable)

        expect:
        wrapper.getPageNumber() == 5
    }

    def "getPageSize should return correct size"() {
        given:
        Pageable pageable = PageRequest.of(2, 50) // size 50
        def wrapper = new PageableWrapper(pageable)

        expect:
        wrapper.getPageSize() == 50
    }

    def "getLastPart edge cases should return input when null or empty"() {
        given:
        PageableWrapper wrapper = new PageableWrapper(PageRequest.of(0, 10))

        expect:
        wrapper.getLastPart(null) == null
        wrapper.getLastPart("") == ""
    }
}