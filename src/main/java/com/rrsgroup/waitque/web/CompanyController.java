package com.rrsgroup.waitque.web;

import com.rrsgroup.waitque.domain.SortDirection;
import com.rrsgroup.waitque.dto.AdminUserDto;
import com.rrsgroup.waitque.dto.CompanyDto;
import com.rrsgroup.waitque.dto.CompanyListDto;
import com.rrsgroup.waitque.dto.UserDto;
import com.rrsgroup.waitque.entity.Company;
import com.rrsgroup.waitque.service.CompanyService;
import com.rrsgroup.waitque.service.DtoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

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

    @GetMapping("/api/internal/companies")
    public CompanyListDto getListOfCompanies(
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "sortField", required = false, defaultValue = "name") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") SortDirection sortDir) {
        Page<Company> pageOfCompanies = companyService.getListOfCompanies(limit, page, sortField, sortDir);

        return mapper.map(pageOfCompanies);
    }

    @GetMapping("/api/internal/companies/{companyId}")
    public CompanyDto getCompany(@PathVariable(name = "companyId") Long companyId) {
        return mapper.map(getCompanySafe(companyId));
    }

    private Company getCompanySafe(Long companyId) {
        Optional<Company> company = companyService.getCompany(companyId);

        if(company.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found with that ID");
        }

        return company.get();
    }

    @PutMapping("/api/internal/companies/{companyId}")
    public CompanyDto updateCompany(@PathVariable(name = "companyId") Long companyId, @RequestBody CompanyDto updateRequest) {
        if(updateRequest.id() != null &&  !updateRequest.id().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The companyId in the URL does not match the companyId in the request body");
        }

        return updateCompanyInternal(companyId, updateRequest);
    }

    private CompanyDto updateCompanyInternal(Long companyId, CompanyDto updateRequest) {
        // Confirm company exists
        getCompanySafe(companyId);

        Company updateCompanyRequest = mapper.map(updateRequest);
        updateCompanyRequest.setId(companyId); // Set the ID in case it wasn't already set

        return mapper.map(companyService.updateCompany(updateCompanyRequest));
    }

    @GetMapping("/api/admin/config/companyInfo")
    public CompanyDto getCompany(@AuthenticationPrincipal AdminUserDto user) {
        return mapper.map(getCompanySafe(user.getCompanyId()));
    }

    @PutMapping("/api/admin/config/companyInfo")
    public CompanyDto updateCompany(@AuthenticationPrincipal AdminUserDto user, @RequestBody CompanyDto updateRequest) {
        Long companyId = user.getCompanyId();

        if(updateRequest.id() != null && !updateRequest.id().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The companyId in the request body is not the user's company");
        }

        return updateCompanyInternal(companyId, updateRequest);
    }
}
