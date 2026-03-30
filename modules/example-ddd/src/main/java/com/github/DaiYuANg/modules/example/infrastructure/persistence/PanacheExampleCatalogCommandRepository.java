package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.modules.example.application.command.CreateExampleProductCommand;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleCatalogCommandRepository;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleProductView;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.mapper.ExampleCatalogCommandMapper;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.mapper.ExampleCatalogReadMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PanacheExampleCatalogCommandRepository implements ExampleCatalogCommandRepository {
  private final ExampleCatalogCommandMapper exampleCatalogCommandMapper;
  private final ExampleCatalogReadMapper exampleCatalogReadMapper;

  @Override
  public ExampleProductView create(@NonNull CreateExampleProductCommand command) {
    val entity = exampleCatalogCommandMapper.toEntity(command);
    entity.persist();
    return exampleCatalogReadMapper.toView(entity);
  }

  @Override
  public void updateStock(@NonNull Long productId, int newStock) {
    val entity =
        ExampleProductEntity.<ExampleProductEntity>findByIdOptional(productId)
            .orElseThrow(() -> new IllegalStateException("product missing after validation"));
    entity.stock = newStock;
  }
}
