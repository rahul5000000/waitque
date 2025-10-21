package com.rrsgroup.company.web;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.common.util.ImageWrapper;
import com.rrsgroup.company.dto.CompanyDto;
import com.rrsgroup.company.dto.CompanyListDto;
import com.rrsgroup.company.dto.QrCodeDto;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.service.CompanyDtoMapper;
import com.rrsgroup.company.service.CompanyService;
import com.rrsgroup.company.service.FrontEndLinkService;
import com.rrsgroup.company.service.QrCodeService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class CompanyController {
    private final CompanyService companyService;
    private final CompanyDtoMapper companyDtoMapper;
    private final QrCodeService qrCodeService;
    private final FrontEndLinkService frontEndLinkService;

    public CompanyController(
            CompanyService companyService,
            CompanyDtoMapper companyDtoMapper,
            QrCodeService qrCodeService,
            FrontEndLinkService frontEndLinkService) {
        this.companyService = companyService;
        this.companyDtoMapper = companyDtoMapper;
        this.qrCodeService = qrCodeService;
        this.frontEndLinkService = frontEndLinkService;
    }

    @PostMapping("/api/internal/companies")
    public CompanyDto createCompany(@RequestBody CompanyDto request) {
        if(request.id() != null) {
            throw new IllegalRequestException("The 'id' field shall not be populated in a create company request");
        }

        if(StringUtils.isBlank(request.name())) {
            throw new IllegalRequestException("The 'name' field shall not be blank in a create company request");
        }

        Company savedCompany = companyService.createCompany(companyDtoMapper.map(request));

        return companyDtoMapper.map(savedCompany);
    }

    @GetMapping("/api/internal/companies")
    public CompanyListDto getListOfCompanies(
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "sortField", required = false, defaultValue = "name") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") SortDirection sortDir) {
        Page<Company> pageOfCompanies = companyService.getListOfCompanies(limit, page, sortField, sortDir);

        return companyDtoMapper.map(pageOfCompanies);
    }

    @GetMapping({"/api/internal/companies/{companyId}", "/api/system/companies/{companyId}"})
    public CompanyDto getCompany(@PathVariable(name = "companyId") Long companyId) {
        return companyDtoMapper.map(getCompanySafe(companyId));
    }

    private Company getCompanySafe(Long companyId) {
        Optional<Company> company = companyService.getCompany(companyId);

        if(company.isEmpty()) {
            throw new RecordNotFoundException("Company not found with that ID");
        }

        return company.get();
    }

    @PutMapping("/api/internal/companies/{companyId}")
    public CompanyDto updateCompany(@PathVariable(name = "companyId") Long companyId, @RequestBody CompanyDto updateRequest) {
        if(updateRequest.id() != null &&  !updateRequest.id().equals(companyId)) {
            throw new IllegalRequestException("The companyId in the URL does not match the companyId in the request body");
        }

        return updateCompanyInternal(companyId, updateRequest);
    }

    private CompanyDto updateCompanyInternal(Long companyId, CompanyDto updateRequest) {
        // Confirm company exists
        getCompanySafe(companyId);

        Company updateCompanyRequest = companyDtoMapper.map(updateRequest);
        updateCompanyRequest.setId(companyId); // Set the ID in case it wasn't already set

        return companyDtoMapper.map(companyService.updateCompany(updateCompanyRequest));
    }

    @GetMapping("/api/admin/config/companyInfo")
    public CompanyDto getCompany(@AuthenticationPrincipal AdminUserDto user) {
        return companyDtoMapper.map(getCompanySafe(user.getCompanyId()));
    }

    @PutMapping("/api/admin/config/companyInfo")
    public CompanyDto updateCompany(@AuthenticationPrincipal AdminUserDto user, @RequestBody CompanyDto updateRequest) {
        Long companyId = user.getCompanyId();

        if(updateRequest.id() != null && !updateRequest.id().equals(companyId)) {
            throw new IllegalUpdateException("The companyId in the request body is not the user's company");
        }

        return updateCompanyInternal(companyId, updateRequest);
    }

    @GetMapping(value = "/api/admin/config/qrcode", produces = "application/zip")
    public void generateAssignableQrCodes(
            @AuthenticationPrincipal AdminUserDto user,
            @RequestParam(name = "count") Integer count,
            @RequestParam(name = "width", required = false, defaultValue = "250") Integer width,
            @RequestParam(name = "height", required = false, defaultValue = "250") Integer height,
            HttpServletResponse response) throws Exception {
        Long companyId = user.getCompanyId();
        Company company = getCompanySafe(companyId);

        if(count < 1) {
            throw new IllegalRequestException("count must be greater than 0");
        }

        List<QrCodeDto> qrCodes = qrCodeService.generateQrCodes(count, company, user);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=qrcodes.zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (int i = 0; i < qrCodes.size(); i++) {
                ImageWrapper image = qrCodeService.generateQRCodeImage(
                        frontEndLinkService.getCustomerLandingPageLink(companyId, qrCodes.get(i).qrCode()),
                        width, 
                        height);

                zos.putNextEntry(new ZipEntry("qrcode-" + (i + 1) + ".png"));
                zos.write(image.toByteArray("png"));
                zos.closeEntry();
            }
        }
    }
}
