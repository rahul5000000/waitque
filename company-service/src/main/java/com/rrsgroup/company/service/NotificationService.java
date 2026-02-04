package com.rrsgroup.company.service;

import com.rrsgroup.common.EmailRequest;
import com.rrsgroup.common.domain.EmailTemplate;
import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.common.entity.Email;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.common.service.EmailService;
import com.rrsgroup.company.domain.EmailStatus;
import com.rrsgroup.company.domain.EmailType;
import com.rrsgroup.company.dto.KeycloakStatus;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.entity.CompanyEmail;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Log4j2
@Service
public class NotificationService {
    private final EmailService emailService;
    private final CompanyService companyService;

    @Autowired
    public NotificationService(EmailService emailService, CompanyService companyService) {
        this.emailService = emailService;
        this.companyService = companyService;
    }

    public void notifyNewUser(
            String newUserEmailAddress,
            String newUserFirstName,
            String newUserLastName,
            String newUserUserName,
            String temporaryPassword,
            AdminUserDto createdBy) {
        try {
            Company company = getCompany(createdBy);
            Email newUserEmail = getEmailByEmailAddress(newUserEmailAddress, newUserFirstName, newUserLastName, company, createdBy);
            Email adminEmail = getEmailByEmailAddress(createdBy.getEmail(), createdBy.getFirstName(), createdBy.getLastName(), company, createdBy);

            String emailHtml = emailService.render(EmailTemplate.NEW_USER,
                    Map.of("companyName", company.getName(),
                            "firstName", newUserFirstName,
                            "username", newUserUserName,
                            "password", temporaryPassword));
            EmailRequest emailRequest = new EmailRequest(newUserEmail, EmailTemplate.NEW_USER, emailHtml);
            emailService.sendEmail(emailRequest);

            String adminEmailHtml = emailService.render(EmailTemplate.NEW_USER_ADMIN,
                    Map.of("userFirstName",newUserFirstName,
                            "userLastName", newUserLastName,
                            "firstName", createdBy.getFirstName()));
            EmailRequest adminEmailRequest = new EmailRequest(adminEmail, EmailTemplate.NEW_USER_ADMIN, adminEmailHtml);
            emailService.sendEmail(adminEmailRequest);
        } catch (Exception e) {
            log.error("Failed to send new user email", e);
            // Swallow exception; there's nothing the customer needs to do about this
        }
    }

    private Email getEmailByEmailAddress(String emailAddress, String firstName, String lastName, Company company, CompanyUserDto userDto) {
        Optional<CompanyEmail> companyEmailOptional = company.findEmailByAddressStatusAndType(emailAddress, EmailStatus.ACTIVE, EmailType.USER);

        if(companyEmailOptional.isPresent()) {
            return companyEmailOptional.get().getEmail();
        }

        return companyService.addEmailToCompany(company, EmailType.USER, firstName, lastName, emailAddress, userDto).getEmail();
    }

    private Company getCompany(CompanyUserDto user) {
        Optional<Company> companyOptional = companyService.getCompany(user.getCompanyId());

        if(companyOptional.isEmpty()) {
            throw new RecordNotFoundException("Company not found for by companyId=" + user.getCompanyId());
        }

        return companyOptional.get();
    }

    public void notifyUserStatusChanged (
            String userEmailAddress,
            String userFirstName,
            String userLastName,
            KeycloakStatus status,
            AdminUserDto createdBy
    ) {
        try {
            Company company = getCompany(createdBy);
            Email newUserEmail = getEmailByEmailAddress(userEmailAddress, userFirstName, userLastName, company, createdBy);
            Email adminEmail = getEmailByEmailAddress(createdBy.getEmail(), createdBy.getFirstName(), createdBy.getLastName(), company, createdBy);

            EmailTemplate template = status == KeycloakStatus.ENABLED ? EmailTemplate.USER_ENABLED : EmailTemplate.USER_DISABLED;
            EmailTemplate adminTemplate = status == KeycloakStatus.ENABLED ? EmailTemplate.USER_ENABLED_ADMIN : EmailTemplate.USER_DISABLED_ADMIN;

            String emailHtml = emailService.render(template,
                    Map.of("firstName", userFirstName));
            EmailRequest emailRequest = new EmailRequest(newUserEmail, template, emailHtml);
            emailService.sendEmail(emailRequest);

            String adminEmailHtml = emailService.render(adminTemplate,
                    Map.of("userFirstName",userFirstName,
                            "userLastName", userLastName,
                            "firstName", createdBy.getFirstName()));
            EmailRequest adminEmailRequest = new EmailRequest(adminEmail, adminTemplate, adminEmailHtml);
            emailService.sendEmail(adminEmailRequest);
        } catch (Exception e) {
            log.error("Failed to send user status changed email", e);
            // Swallow exception; there's nothing the customer needs to do about this
        }
    }

    public void notifyUserDeleted (
            String userFirstName,
            String userLastName,
            AdminUserDto createdBy
    ) {
        try {
            Company company = getCompany(createdBy);
            Email adminEmail = getEmailByEmailAddress(createdBy.getEmail(), createdBy.getFirstName(), createdBy.getLastName(), company, createdBy);

            String adminEmailHtml = emailService.render(EmailTemplate.USER_DELETED_ADMIN,
                    Map.of("userFirstName",userFirstName,
                            "userLastName", userLastName,
                            "firstName", createdBy.getFirstName()));
            EmailRequest adminEmailRequest = new EmailRequest(adminEmail, EmailTemplate.USER_DELETED_ADMIN, adminEmailHtml);
            emailService.sendEmail(adminEmailRequest);
        } catch (Exception e) {
            log.error("Failed to send user status changed email", e);
            // Swallow exception; there's nothing the customer needs to do about this
        }
    }

    public void notifyPasswordReset (
            String userEmailAddress,
            String userFirstName,
            String userLastName,
            String temporaryPassword,
            AdminUserDto createdBy
    ) {
        try {
            Company company = getCompany(createdBy);
            Email newUserEmail = getEmailByEmailAddress(userEmailAddress, userFirstName, userLastName, company, createdBy);
            Email adminEmail = getEmailByEmailAddress(createdBy.getEmail(), createdBy.getFirstName(), createdBy.getLastName(), company, createdBy);

            String emailHtml = emailService.render(EmailTemplate.RESET_PASSWORD,
                    Map.of("firstName", userFirstName,
                            "password", temporaryPassword));
            EmailRequest emailRequest = new EmailRequest(newUserEmail, EmailTemplate.RESET_PASSWORD, emailHtml);
            emailService.sendEmail(emailRequest);

            String adminEmailHtml = emailService.render(EmailTemplate.RESET_PASSWORD_ADMIN,
                    Map.of("userFirstName",userFirstName,
                            "userLastName", userLastName,
                            "firstName", createdBy.getFirstName()));
            EmailRequest adminEmailRequest = new EmailRequest(adminEmail, EmailTemplate.RESET_PASSWORD_ADMIN, adminEmailHtml);
            emailService.sendEmail(adminEmailRequest);
        } catch (Exception e) {
            log.error("Failed to send user password reset email", e);
            // Swallow exception; there's nothing the customer needs to do about this
        }
    }
}
