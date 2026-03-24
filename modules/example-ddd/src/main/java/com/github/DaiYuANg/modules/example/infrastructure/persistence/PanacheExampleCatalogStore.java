package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.modules.example.application.dto.CreateExampleProductCommand;
import com.github.DaiYuANg.modules.example.application.dto.ExampleProductView;
import com.github.DaiYuANg.modules.example.application.port.ExampleCatalogStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PanacheExampleCatalogStore implements ExampleCatalogStore {

  @Override
  public ExampleProductView create(CreateExampleProductCommand command) {
    var e = new ExampleProductEntity();
    e.name = command.name();
    e.priceMinor = command.priceMinor();
    e.stock = command.stock();
    e.active = true;
    e.persist();
    return toView(e);
  }

  @Override
  public List<ExampleProductView> listActive() {
    return ExampleProductEntity.<ExampleProductEntity>list("active", true).stream()
        .map(this::toView)
        .toList();
  }

  @Override
  public Optional<ExampleProductView> findById(Long id) {
    return ExampleProductEntity.<ExampleProductEntity>findByIdOptional(id).map(this::toView);
  }

  @Override
  public void updateStock(Long productId, int newStock) {
    var e =
        ExampleProductEntity.<ExampleProductEntity>findByIdOptional(productId)
            .orElseThrow(() -> new IllegalStateException("product missing after validation"));
    e.stock = newStock;
  }

  private ExampleProductView toView(ExampleProductEntity e) {
    return new ExampleProductView(e.id, e.name, e.priceMinor, e.stock, e.active);
  }
}
