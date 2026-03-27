package com.github.DaiYuANg.modules.example.application.port.driven;

import com.github.DaiYuANg.modules.example.application.readmodel.ExampleProductView;
import com.github.DaiYuANg.modules.example.domain.model.catalog.ExampleProductSnapshot;
import java.util.List;
import java.util.Optional;

public interface ExampleCatalogReadRepository {

  List<ExampleProductView> listActive();

  Optional<ExampleProductSnapshot> findSnapshotById(Long id);
}
