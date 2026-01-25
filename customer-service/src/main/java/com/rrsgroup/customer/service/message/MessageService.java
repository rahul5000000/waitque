package com.rrsgroup.customer.service.message;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.customer.domain.message.MessageStatus;
import com.rrsgroup.customer.entity.message.Message;
import com.rrsgroup.customer.repository.MessageRepository;
import com.rrsgroup.customer.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final EventService eventService;

    @Autowired
    public MessageService(MessageRepository messageRepository, EventService eventService) {
        this.messageRepository = messageRepository;
        this.eventService = eventService;
    }

    public Message saveMessageAnonymous(Message message) {
        LocalDateTime now = LocalDateTime.now();
        String createdBy = "anonymous";

        message.setStatus(MessageStatus.UNREAD);
        message.setCreatedDate(now);
        message.setUpdatedDate(now);
        message.setCreatedBy(createdBy);
        message.setUpdatedBy(createdBy);

        Message savedMessage = messageRepository.save(message);

        eventService.messageSent(savedMessage);

        return savedMessage;
    }

    public Page<Message> getCompanyListOfMessages(Long companyId, List<MessageStatus> statuses, int limit, int page, String sortField, SortDirection sortDir) {
        Pageable pageable = PageRequest.of(
                page,
                limit,
                sortDir == SortDirection.ASC ? Sort.by(sortField).ascending() : Sort.by(sortField).descending());

        if(statuses == null || statuses.isEmpty()) {
            return messageRepository.findByCompanyId(companyId, pageable);
        } else {
            return messageRepository.findByCompanyIdAndStatusIn(companyId, statuses, pageable);
        }
    }

    public Optional<Message> getMessageById(Long id, CompanyUserDto userDto) {
        return messageRepository.findByIdAndCustomer_CrmConfig_CompanyId(id, userDto.getCompanyId());
    }
}
