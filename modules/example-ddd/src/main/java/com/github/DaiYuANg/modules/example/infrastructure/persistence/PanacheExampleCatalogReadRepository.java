package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.modules.example.application.port.driven.ExampleCatalogReadRepository;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleProductView;
import com.github.DaiYuANg.modules.example.domain.model.catalog.ExampleProductSnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PanacheExampleCatalogReadRepository implements ExampleCatalogReadRepository {

  @Override
  public List<ExampleProductView> listActive() {
    return ExampleProductEntity.<ExampleProductEntity>list("active", true).stream()
        .map(this::toView)
        .toList();
  }

  @Override
  public Optional<ExampleProductSnapshot> findSnapshotById(Long id) {
    return ExampleProductEntity.<ExampleProductEntity>findByIdOptional(id).map(this::toSnapshot);
  }

  private ExampleProductView toView(ExampleProductEntity entity) {
    return new ExampleProductView(
        entity.id, entity.name, entity.priceMinor, entity.stock, entity.active);
  }

  private ExampleProductSnapshot toSnapshot(ExampleProductEntity entity) {
    return new ExampleProductSnapshot(
        entity.id, entity.name, entity.priceMinor, entity.stock, entity.active);
  }
}
