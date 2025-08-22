package com.rrsgroup.waitque.web;

import com.rrsgroup.waitque.dto.CompanyDto;
import com.rrsgroup.waitque.entity.Company;
import com.rrsgroup.waitque.service.CompanyService;
import com.rrsgroup.waitque.service.DtoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CompanyController {
    private final CompanyService companyService;
    private final DtoMapper mapper;

    public CompanyController(CompanyService companyService, DtoMapper mapper) {
        this.companyService = companyService;
        this.mapper = mapper;
    }

    @PostMapping("/api/internal/companies")
    public CompanyDto createCompany(@RequestBody CompanyDto request) {
        if(request.id() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The 'id' field shall not be populated in a create company request");
        }

        if(StringUtils.isBlank(request.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The 'name' field shall not be blank in a create company request");
        }

        Company savedCompany = companyService.createCompany(mapper.map(request));

        return mapper.map(savedCompany);
    }
}
