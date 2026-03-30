package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.modules.example.application.port.driven.ExampleOrderCommandRepository;
import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrder;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.mapper.ExampleOrderCommandMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PanacheExampleOrderCommandRepository implements ExampleOrderCommandRepository {
  private final ExampleOrderCommandMapper exampleOrderCommandMapper;

  @Override
  public ExampleOrder save(@NonNull ExampleOrder order) {
    val entity = exampleOrderCommandMapper.toEntity(order);
    order.lines().stream()
        .map(line -> exampleOrderCommandMapper.toEntity(line, entity))
        .forEach(entity.lines::add);
    entity.persist();
    return order.persisted(entity.id);
  }
}
