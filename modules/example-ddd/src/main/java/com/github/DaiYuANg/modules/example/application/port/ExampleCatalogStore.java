package com.github.DaiYuANg.modules.example.application.port;

import com.github.DaiYuANg.modules.example.application.dto.CreateExampleProductCommand;
import com.github.DaiYuANg.modules.example.application.dto.ExampleProductView;
import java.util.List;
import java.util.Optional;

public interface ExampleCatalogStore {

  ExampleProductView create(CreateExampleProductCommand command);

  List<ExampleProductView> listActive();

  Optional<ExampleProductView> findById(Long id);

  void updateStock(Long productId, int newStock);
}
