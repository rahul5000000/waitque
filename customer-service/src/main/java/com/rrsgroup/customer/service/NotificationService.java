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
import com.rrsgroup.customer.entity.Customer;
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

    @Autowired
    public NotificationService(
            EmailService emailService,
            CustomerCrmIntegrationService integrationService,
            CompanyService companyService,
            CommonDtoMapper commonDtoMapper) {
        this.emailService = emailService;
        this.integrationService = integrationService;
        this.companyService = companyService;
        this.commonDtoMapper = commonDtoMapper;
    }

    public void sendNotification(Message message, Customer customer) {
        CompanyDto companyDto = getCompanyForCustomer(customer);

        if(companyDto.messageNotificationEmail() != null) {
            try {
                CrmCustomer crmCustomer = getCrmCustomerForCustomer(customer);
                String customerName = getCustomerName(crmCustomer);
                Email messageNotificationEmail = commonDtoMapper.map(companyDto.messageNotificationEmail());
                String emailHtml = emailService.render(EmailTemplate.NEW_MESSAGE,
                        Map.of("firstName", messageNotificationEmail.getFirstName(),
                                "year", LocalDate.now().getYear(),
                                "customerName", customerName,
                                "messageDate", message.getCreatedDate(),
                                "message", message.getMessage()));
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
