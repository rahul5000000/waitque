package com.rrsgroup.customer.web;

import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.customer.dto.message.MessageDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.message.Message;
import com.rrsgroup.customer.service.CustomerService;
import com.rrsgroup.customer.service.message.MessageDtoMapper;
import com.rrsgroup.customer.service.message.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class MessageController {
    private final CustomerService customerService;
    private final MessageDtoMapper messageDtoMapper;
    private final MessageService messageService;

    @Autowired
    public MessageController(CustomerService customerService, MessageDtoMapper messageDtoMapper, MessageService messageService) {
        this.customerService = customerService;
        this.messageDtoMapper = messageDtoMapper;
        this.messageService = messageService;
    }

    @PostMapping("/api/public/customers/{customerId}/messages")
    public MessageDto sendMessage(@PathVariable("customerId") Long customerId, @RequestBody MessageDto request) {
        // Get customer for company; validate customer exists
        Optional<Customer> customerOptional = customerService.getCustomerById(customerId, request.companyId());

        if(customerOptional.isEmpty()) {
            throw new RecordNotFoundException("Customer does not exist by customerId=" + customerId + ", companyId=" + request.companyId());
        }

        Message message = messageDtoMapper.map(request, customerOptional.get());
        return messageDtoMapper.map(messageService.saveMessageAnonymous(message));
    }
}
