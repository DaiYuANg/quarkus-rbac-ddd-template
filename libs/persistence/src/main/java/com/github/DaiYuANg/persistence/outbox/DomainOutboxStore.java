package com.github.DaiYuANg.persistence.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.DaiYuANg.common.domain.DomainEvent;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Stores serialized domain events in the same transaction as the write-side aggregate change.
 *
 * <p>This is intentionally minimal: dispatch/retry workers can be added later without changing the
 * write-side contract.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DomainOutboxStore implements PanacheRepository<OutboxMessageEntity> {

  private final ObjectMapper objectMapper;

  public void append(
      @NonNull String aggregateType, @NonNull Object aggregateId, @NonNull DomainEvent event) {
    val entity = new OutboxMessageEntity();
    entity.aggregateType = aggregateType;
    entity.aggregateId = String.valueOf(aggregateId);
    entity.eventType = event.eventType();
    entity.payload = toJson(event);
    entity.occurredAt = event.occurredAt();
    entity.availableAt = entity.occurredAt != null ? entity.occurredAt : Instant.now();
    persist(entity);
  }

  private String toJson(DomainEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("failed to serialize domain event " + event.eventType(), e);
    }
  }
}
