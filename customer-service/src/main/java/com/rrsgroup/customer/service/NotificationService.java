package com.rrsgroup.customer.service;

import com.rrsgroup.common.EmailRequest;
import com.rrsgroup.common.domain.EmailTemplate;
import com.rrsgroup.common.entity.Email;
import com.rrsgroup.common.exception.EmailSendException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.common.service.CommonDtoMapper;
import com.rrsgroup.common.service.EmailService;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.CrmCustomerType;
import com.rrsgroup.customer.dto.CompanyDto;
import com.rrsgroup.customer.dto.LeadFlowDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.entity.message.Message;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@Log4j2
public class NotificationService {
    private final EmailService emailService;
    private final CustomerCrmIntegrationService integrationService;
    private final CompanyService companyService;
    private final CommonDtoMapper commonDtoMapper;
    private final LeadFlowService leadFlowService;

    @Autowired
    public NotificationService(
            EmailService emailService,
            CustomerCrmIntegrationService integrationService,
            CompanyService companyService,
            CommonDtoMapper commonDtoMapper,
            LeadFlowService leadFlowService) {
        this.emailService = emailService;
        this.integrationService = integrationService;
        this.companyService = companyService;
        this.commonDtoMapper = commonDtoMapper;
        this.leadFlowService = leadFlowService;
    }

    public void sendNotification(Message message, Customer customer) {
        CompanyDto companyDto = getCompanyForCustomer(customer);

        if(companyDto.messageNotificationEmail() != null) {
            try {
                CrmCustomer crmCustomer = getCrmCustomerForCustomer(customer);
                String customerName = getCustomerName(crmCustomer);
                Email messageNotificationEmail = commonDtoMapper.map(companyDto.messageNotificationEmail());
                String messageUrl = companyService.getMessageFrontEndLink(companyDto.id(), message.getId()).orElse(null);
                String emailHtml = emailService.render(EmailTemplate.NEW_MESSAGE,
                        Map.of("firstName", messageNotificationEmail.getFirstName(),
                                "year", LocalDate.now().getYear(),
                                "customerName", customerName,
                                "messageDate", message.getCreatedDate(),
                                "message", message.getMessage(),
                                "messageUrl", messageUrl));
                EmailRequest emailRequest = new EmailRequest(messageNotificationEmail, EmailTemplate.NEW_MESSAGE, emailHtml);
                emailService.sendEmail(emailRequest);
            } catch (Exception e) {
                log.error("Failed to send new message email", e);
                // Swallow exception; there's nothing the customer needs to do about this
            }
        } else {
            log.warn("companyId={} is not configured with an message notification email address", companyDto.id());
        }
    }

    public void sendNotification(Lead lead, LeadFlowDto leadFlow, Customer customer) {
        CompanyDto companyDto = getCompanyForCustomer(customer);

        if(companyDto.leadNotificationEmail() != null) {
            try {
                CrmCustomer crmCustomer = getCrmCustomerForCustomer(customer);
                String customerName = getCustomerName(crmCustomer);
                Email leadNotificationEmail = commonDtoMapper.map(companyDto.leadNotificationEmail());
                String leadUrl = companyService.getLeadFrontEndLink(companyDto.id(), lead.getId()).orElse(null);
                String emailHtml = emailService.render(EmailTemplate.NEW_LEAD,
                        Map.of("firstName", leadNotificationEmail.getFirstName(),
                                "year", LocalDate.now().getYear(),
                                "customerName", customerName,
                                "leadCreatedDate", lead.getCreatedDate(),
                                "leadFlowName", leadFlow.name(),
                                "leadUrl", leadUrl));
                EmailRequest emailRequest = new EmailRequest(leadNotificationEmail, EmailTemplate.NEW_LEAD, emailHtml);
                emailService.sendEmail(emailRequest);
            } catch (Exception e) {
                log.error("Failed to send new lead email", e);
                // Swallow exception; there's nothing the customer needs to do about this
            }
        } else {
            log.warn("companyId={} is not configured with a lead notification email address", companyDto.id());
        }
    }

    private CompanyDto getCompanyForCustomer(Customer customer) {
        Optional<CompanyDto> companyOptional = companyService.getCompany(customer.getCrmConfig().getCompanyId());

        if(companyOptional.isEmpty()) {
            throw new RecordNotFoundException("Company not found for customerId" + customer.getId());
        }

        return companyOptional.get();
    }

    private CrmCustomer getCrmCustomerForCustomer(Customer customer) {
        Optional<CrmCustomer> crmCustomerOptional = integrationService.getCrmCustomer(customer.getCrmCustomerId(), customer.getCrmConfig());

        if(crmCustomerOptional.isEmpty()) {
            throw new RecordNotFoundException("CRM customer information not found for customerId=" + customer.getId());
        }

        return crmCustomerOptional.get();
    }

    private String getCustomerName(CrmCustomer crmCustomer) {
        return switch (crmCustomer.getCustomerType()) {
            case COMMERCIAL -> crmCustomer.getCompanyName();
            case RESIDENTIAL -> crmCustomer.getFirstName() + " " + crmCustomer.getLastName();
            default -> throw new RuntimeException("Unknown CRM Customer Type");
        };
    }
}
