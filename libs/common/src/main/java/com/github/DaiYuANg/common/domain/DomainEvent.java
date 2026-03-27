package com.github.DaiYuANg.common.domain;

import java.time.Instant;

/**
 * Minimal domain-event contract for write-side use cases that want to persist an outbox record in
 * the same transaction.
 */
public interface DomainEvent {

  String eventType();

  Instant occurredAt();
}
