package com.rrsgroup.customer.service.message;

import com.rrsgroup.customer.domain.lead.LeadStatus;
import com.rrsgroup.customer.domain.message.MessageStatus;
import com.rrsgroup.customer.entity.message.Message;
import com.rrsgroup.customer.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message saveMessageAnonymous(Message message) {
        LocalDateTime now = LocalDateTime.now();
        String createdBy = "anonymous";

        message.setStatus(MessageStatus.UNREAD);
        message.setCreatedDate(now);
        message.setUpdatedDate(now);
        message.setCreatedBy(createdBy);
        message.setUpdatedBy(createdBy);

        return messageRepository.save(message);
    }
}
