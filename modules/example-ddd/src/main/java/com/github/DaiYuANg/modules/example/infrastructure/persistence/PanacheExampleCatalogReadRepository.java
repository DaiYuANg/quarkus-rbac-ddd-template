package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.modules.example.application.port.driven.ExampleCatalogReadRepository;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleProductView;
import com.github.DaiYuANg.modules.example.domain.model.catalog.ExampleProductSnapshot;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.mapper.ExampleCatalogReadMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PanacheExampleCatalogReadRepository implements ExampleCatalogReadRepository {
  private final ExampleCatalogReadMapper exampleCatalogReadMapper;

  @Override
  public List<ExampleProductView> listActive() {
    return ExampleProductEntity.<ExampleProductEntity>list("active", true).stream()
        .map(exampleCatalogReadMapper::toView)
        .toList();
  }

  @Override
  public Optional<ExampleProductSnapshot> findSnapshotById(Long id) {
    return ExampleProductEntity.<ExampleProductEntity>findByIdOptional(id)
        .map(exampleCatalogReadMapper::toSnapshot);
  }
}
