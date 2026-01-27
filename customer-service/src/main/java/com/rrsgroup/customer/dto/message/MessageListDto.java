package com.rrsgroup.customer.dto.message;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.PaginatedDto;
import com.rrsgroup.customer.domain.message.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageListDto extends PaginatedDto {
    private List<MessageListItem> messages;

    public MessageListDto(Integer page, Integer limit, Long total, String sortField, SortDirection sortDir, List<MessageListItem> messages) {
        super(page, limit, total, sortField, sortDir);
        this.messages = messages;
    }

    @Data
    @AllArgsConstructor
    public static class MessageListItem {
        private Long id;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String email;
        private String contentSnippet;
        private MessageStatus status;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;
    }
}