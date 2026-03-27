package com.github.DaiYuANg.modules.example.domain.model.order;

import com.github.DaiYuANg.common.domain.DomainEvent;
import java.time.Instant;

public record ExampleOrderCreatedEvent(
    Long orderId, String buyerUsername, long totalMinor, int lineCount, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String eventType() {
    return "example.order.created";
  }
}
