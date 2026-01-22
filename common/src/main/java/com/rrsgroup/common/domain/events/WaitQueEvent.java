package com.rrsgroup.common.domain.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class WaitQueEvent {
    protected Long companyId;
    protected Long customerId;
    protected String userId;
    protected EventType eventType;
}
