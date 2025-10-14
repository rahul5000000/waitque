package com.rrsgroup.customer.web;

import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.customer.dto.message.MessageDto;
import com.rrsgroup.customer.entity.QrCode;
import com.rrsgroup.customer.entity.message.Message;
import com.rrsgroup.customer.service.QrCodeService;
import com.rrsgroup.customer.service.message.MessageDtoMapper;
import com.rrsgroup.customer.service.message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
public class MessageController {
    private final MessageDtoMapper messageDtoMapper;
    private final MessageService messageService;
    private final QrCodeService qrCodeService;

    @Autowired
    public MessageController(
            MessageDtoMapper messageDtoMapper,
            MessageService messageService,
            QrCodeService qrCodeService) {
        this.messageDtoMapper = messageDtoMapper;
        this.messageService = messageService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/api/public/customers/qrCode/{qrCode}/messages")
    public MessageDto sendMessage(@PathVariable("qrCode") UUID qrCode, @RequestBody MessageDto request) {
        Optional<QrCode> qrCodeOptional = qrCodeService.getAssociatedQrCode(qrCode);

        if(qrCodeOptional.isEmpty()) {
            throw new RecordNotFoundException("Customer does not exist with qrCode=" + qrCode);
        }

        Message message = messageDtoMapper.map(request, qrCodeOptional.get().getCustomer());
        return messageDtoMapper.map(messageService.saveMessageAnonymous(message));
    }
}
