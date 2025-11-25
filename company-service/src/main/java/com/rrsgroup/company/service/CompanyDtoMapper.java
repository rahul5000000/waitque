package com.rrsgroup.company.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.common.service.CommonDtoMapper;
import com.rrsgroup.company.dto.CompanyDto;
import com.rrsgroup.company.dto.CompanyListDto;
import com.rrsgroup.company.entity.Company;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CompanyDtoMapper {
    @Value("${CDN_BASE_URL}")
    private String cdnBaseUrl;

    private final CommonDtoMapper commonDtoMapper;

    @Autowired
    public CompanyDtoMapper(CommonDtoMapper commonDtoMapper) {
        this.commonDtoMapper = commonDtoMapper;
    }

    public CompanyDto map(final Company company) {
        AddressDto addressDto = commonDtoMapper.map(company.getAddress());
        PhoneNumberDto phoneNumberDto = commonDtoMapper.map(company.getPhoneNumber());
        String logoUrl = null;
        if(StringUtils.isNotEmpty(company.getLogoUrl())) {
            logoUrl = cdnBaseUrl + "/" + company.getLogoUrl();
        }

        return new CompanyDto(company.getId(), company.getName(), addressDto, phoneNumberDto, logoUrl,
                company.getLandingPrompt(), company.getTextColor(), company.getBackgroundColor(),
                company.getPrimaryButtonColor(), company.getSecondaryButtonColor(), company.getWarningButtonColor(),
                company.getDangerButtonColor());
    }

    public Company map(final CompanyDto dto) {
        return new Company(dto.id(), dto.name(), dto.logoUrl(), dto.landingPrompt(), dto.textColor(),
                dto.backgroundColor(), dto.primaryButtonColor(), dto.secondaryButtonColor(),
                dto.warningButtonColor(), dto.dangerButtonColor(), commonDtoMapper.map(dto.address()), commonDtoMapper.map(dto.phoneNumber()));
    }

    public CompanyListDto map(final Page<Company> pageOfCompanies) {
        String sortField = pageOfCompanies.getPageable().getSort().stream().findFirst()
                .map(Sort.Order::getProperty).orElse("");
        SortDirection sortDir = pageOfCompanies.getPageable().getSort().stream().findFirst()
                .map(sort -> sort.getDirection() == Sort.Direction.ASC ? SortDirection.ASC : SortDirection.DESC).orElse(SortDirection.ASC);
        return new CompanyListDto(
                pageOfCompanies.getPageable().getPageNumber(),
                pageOfCompanies.getPageable().getPageSize(),
                pageOfCompanies.getTotalElements(),
                sortField,
                sortDir,
                pageOfCompanies.getContent().stream().map(company -> new CompanyListDto.CompanyListItem(company.getId(), company.getName(), company.getLogoUrl())).toList()
        );
    }
}
