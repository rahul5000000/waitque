package com.rrsgroup.customer.web;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.entity.Email;
import com.rrsgroup.common.exception.EmailSendException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.common.service.CommonDtoMapper;
import com.rrsgroup.common.service.EmailService;
import com.rrsgroup.customer.domain.message.MessageStatus;
import com.rrsgroup.customer.dto.CompanyDto;
import com.rrsgroup.customer.dto.message.MessageDto;
import com.rrsgroup.customer.dto.message.MessageListDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.QrCode;
import com.rrsgroup.customer.entity.message.Message;
import com.rrsgroup.customer.service.CompanyService;
import com.rrsgroup.customer.service.QrCodeService;
import com.rrsgroup.customer.service.message.MessageDtoMapper;
import com.rrsgroup.customer.service.message.MessageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Log4j2
public class MessageController {
    private final MessageDtoMapper messageDtoMapper;
    private final MessageService messageService;
    private final QrCodeService qrCodeService;
    private final EmailService emailService;
    private final CompanyService companyService;
    private final CommonDtoMapper commonDtoMapper;

    @Autowired
    public MessageController(
            MessageDtoMapper messageDtoMapper,
            MessageService messageService,
            QrCodeService qrCodeService,
            EmailService emailService,
            CompanyService companyService,
            CommonDtoMapper commonDtoMapper) {
        this.messageDtoMapper = messageDtoMapper;
        this.messageService = messageService;
        this.qrCodeService = qrCodeService;
        this.emailService = emailService;
        this.companyService = companyService;
        this.commonDtoMapper = commonDtoMapper;
    }

    @PostMapping("/api/public/customers/qrCode/{qrCode}/messages")
    public MessageDto sendMessage(@PathVariable("qrCode") UUID qrCode, @RequestBody MessageDto request) {
        Optional<QrCode> qrCodeOptional = qrCodeService.getAssociatedQrCode(qrCode);

        if(qrCodeOptional.isEmpty()) {
            throw new RecordNotFoundException("Customer does not exist with qrCode=" + qrCode);
        }

        Customer customer = qrCodeOptional.get().getCustomer();
        CompanyDto companyDto = getCompanyForCustomer(customer);

        Message message = messageDtoMapper.map(request, customer);
        MessageDto response = messageDtoMapper.map(messageService.saveMessageAnonymous(message));

        if(companyDto.messageNotificationEmail() != null) {
            try {
                Email messageNotificationEmail = commonDtoMapper.map(companyDto.messageNotificationEmail());
                emailService.sendEmail(request.message(), messageNotificationEmail);
            } catch (EmailSendException e) {
                log.error("Failed to send new message email", e);
                // Swallow exception; there's nothing the customer needs to do about this
            }
        } else {
            log.warn("companyId={} is not configured with an message notification email address", companyDto.id());
        }

        return response;
    }

    private CompanyDto getCompanyForCustomer(Customer customer) {
        Optional<CompanyDto> companyOptional = companyService.getCompany(customer.getCrmConfig().getCompanyId());

        if(companyOptional.isEmpty()) {
            throw new RecordNotFoundException("Company not found for customerId" + customer.getId());
        }

        return companyOptional.get();
    }

    @GetMapping("/api/admin/messages")
    public MessageListDto getListOfMessages(
            @AuthenticationPrincipal AdminUserDto user,
            @RequestParam(name = "status", required = false) List<MessageStatus> statuses,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "sortField", required = false, defaultValue = "id") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "DESC") SortDirection sortDir) {
        Long companyId = user.getCompanyId();
        Page<Message> pageOfMessages = messageService.getCompanyListOfMessages(companyId, statuses, limit, page, sortField, sortDir);

        return messageDtoMapper.map(pageOfMessages);
    }

    @GetMapping("/api/admin/messages/{messageId}")
    public MessageDto getMessage(@AuthenticationPrincipal AdminUserDto user, @PathVariable("messageId") Long messageId) {
        Optional<Message> messageOptional = messageService.getMessageById(messageId, user);

        if(messageOptional.isEmpty()) {
            throw new RecordNotFoundException("Message not found by messageId=" + messageId);
        }

        return messageDtoMapper.map(messageOptional.get());
    }
}
