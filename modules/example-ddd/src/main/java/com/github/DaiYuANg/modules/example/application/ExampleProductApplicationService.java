package com.github.DaiYuANg.modules.example.application;

import com.github.DaiYuANg.modules.example.application.dto.CreateExampleProductCommand;
import com.github.DaiYuANg.modules.example.application.dto.ExampleProductView;
import com.github.DaiYuANg.modules.example.application.port.ExampleCatalogStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExampleProductApplicationService {

  private final ExampleCatalogStore catalogStore;

  @Transactional
  public ExampleProductView create(CreateExampleProductCommand command) {
    return catalogStore.create(command);
  }

  public List<ExampleProductView> listActive() {
    return catalogStore.listActive();
  }
}
