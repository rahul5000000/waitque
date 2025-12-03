package com.rrsgroup.company.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.EmailDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.common.entity.Email;
import com.rrsgroup.common.service.CommonDtoMapper;
import com.rrsgroup.company.domain.EmailStatus;
import com.rrsgroup.company.domain.EmailType;
import com.rrsgroup.company.dto.CompanyDto;
import com.rrsgroup.company.dto.CompanyListDto;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.entity.CompanyEmail;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

        EmailDto messageNotificationEmail = getEmailByTypeAndStatus(company, EmailType.MESSAGE_NOTIFICATION, EmailStatus.ACTIVE);
        EmailDto leadNotificationEmail = getEmailByTypeAndStatus(company, EmailType.LEAD_NOTIFICATION, EmailStatus.ACTIVE);

        return new CompanyDto(company.getId(), company.getName(), addressDto, phoneNumberDto, logoUrl,
                company.getLandingPrompt(), company.getTextColor(), company.getBackgroundColor(),
                company.getPrimaryButtonColor(), company.getSecondaryButtonColor(), company.getWarningButtonColor(),
                company.getDangerButtonColor(), messageNotificationEmail, leadNotificationEmail);
    }

    private EmailDto getEmailByTypeAndStatus(Company company, EmailType type, EmailStatus status) {
        return company.getEmails().stream().filter(email -> email.getType().equals(type) && email.getStatus().equals(status))
                .findFirst().map(email -> commonDtoMapper.map(email.getEmail())).orElse(null);
    }

    public Company map(final CompanyDto dto) {
        List<CompanyEmail> emails = new ArrayList<>();

        if(dto.messageNotificationEmail() != null) {
            emails.add(map(dto.messageNotificationEmail(), EmailType.MESSAGE_NOTIFICATION));
        }

        if(dto.leadNotificationEmail() != null) {
            emails.add(map(dto.leadNotificationEmail(), EmailType.LEAD_NOTIFICATION));
        }

        Company company = new Company(dto.id(), dto.name(), dto.logoUrl(), dto.landingPrompt(), dto.textColor(),
                dto.backgroundColor(), dto.primaryButtonColor(), dto.secondaryButtonColor(),
                dto.warningButtonColor(), dto.dangerButtonColor(), commonDtoMapper.map(dto.address()),
                commonDtoMapper.map(dto.phoneNumber()), emails);

        emails.stream().forEach(email -> email.setCompany(company));

        return company;
    }

    private CompanyEmail map(EmailDto dto, EmailType type) {
        return CompanyEmail.builder().type(type).status(EmailStatus.ACTIVE).email(Email.builder().email(dto.email())
                        .firstName(dto.firstName()).lastName(dto.lastName()).build()).build();
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
