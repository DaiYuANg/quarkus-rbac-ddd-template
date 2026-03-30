package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.modules.example.application.command.CreateExampleProductCommand;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleCatalogCommandRepository;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleProductView;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.val;

@ApplicationScoped
public class PanacheExampleCatalogCommandRepository implements ExampleCatalogCommandRepository {

  @Override
  public ExampleProductView create(CreateExampleProductCommand command) {
    val entity = new ExampleProductEntity();
    entity.name = command.name();
    entity.priceMinor = command.priceMinor();
    entity.stock = command.stock();
    entity.active = true;
    entity.persist();
    return new ExampleProductView(
        entity.id, entity.name, entity.priceMinor, entity.stock, entity.active);
  }

  @Override
  public void updateStock(Long productId, int newStock) {
    val entity =
        ExampleProductEntity.<ExampleProductEntity>findByIdOptional(productId)
            .orElseThrow(() -> new IllegalStateException("product missing after validation"));
    entity.stock = newStock;
  }
}
