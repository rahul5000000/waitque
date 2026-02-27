package com.rrsgroup.customer.entity.event;

import com.rrsgroup.common.domain.events.EventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;


@Entity
@Table(name = "generic_event", schema = "event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericEvent {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    // Event classification
    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(name = "event_version", nullable = false)
    @Builder.Default
    private Integer eventVersion = 1;

    @Column(name = "company_id")
    private Long companyId;

    // SNS / idempotency support
    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "external_event_id")
    private String externalEventId;

    // Event payload (JSONB)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload;

    // Timing
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
