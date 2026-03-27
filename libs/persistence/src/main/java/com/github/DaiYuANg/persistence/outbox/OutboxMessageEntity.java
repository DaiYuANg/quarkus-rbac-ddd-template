package com.github.DaiYuANg.persistence.outbox;

import com.github.DaiYuANg.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "app_outbox_message")
public class OutboxMessageEntity extends BaseEntity {

  @Column(name = "aggregate_type", nullable = false, length = 128)
  public String aggregateType;

  @Column(name = "aggregate_id", nullable = false, length = 128)
  public String aggregateId;

  @Column(name = "event_type", nullable = false, length = 255)
  public String eventType;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  public String payload;

  @Column(name = "occurred_at", nullable = false)
  public Instant occurredAt;

  @Column(name = "available_at", nullable = false)
  public Instant availableAt;

  @Column(name = "published_at")
  public Instant publishedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  public OutboxStatus status = OutboxStatus.PENDING;
}
