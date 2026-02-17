package com.rrsgroup.common.domain.events;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class WaitQueEvent {
    protected Long companyId;
    protected Long customerId;
    protected String userId;
    protected EventType eventType;

    @Builder.Default
    protected LocalDateTime occurredAt = LocalDateTime.now();
}
