package com.github.DaiYuANg.modules.example.application.port.driven;

import com.github.DaiYuANg.modules.example.application.command.CreateExampleProductCommand;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleProductView;

public interface ExampleCatalogCommandRepository {

  ExampleProductView create(CreateExampleProductCommand command);

  void updateStock(Long productId, int newStock);
}
