package com.github.DaiYuANg.modules.example.domain.model.order;

import java.time.Instant;
import java.util.List;

public record ExampleOrder(
    Long id,
    String buyerUsername,
    ExampleOrderStatus status,
    long totalMinor,
    List<ExampleOrderLine> lines) {

  public ExampleOrder {
    lines = List.copyOf(lines);
  }

  public static ExampleOrder place(String buyerUsername, List<ExampleOrderLine> lines) {
    var safeLines = List.copyOf(lines);
    var totalMinor = safeLines.stream().mapToLong(ExampleOrderLine::lineTotalMinor).sum();
    return new ExampleOrder(null, buyerUsername, ExampleOrderStatus.CREATED, totalMinor, safeLines);
  }

  public ExampleOrder persisted(Long persistedId) {
    return new ExampleOrder(persistedId, buyerUsername, status, totalMinor, lines);
  }

  public ExampleOrderCreatedEvent placedEvent() {
    if (id == null) {
      throw new IllegalStateException("order id must exist before publishing an outbox event");
    }
    return new ExampleOrderCreatedEvent(id, buyerUsername, totalMinor, lines.size(), Instant.now());
  }
}
