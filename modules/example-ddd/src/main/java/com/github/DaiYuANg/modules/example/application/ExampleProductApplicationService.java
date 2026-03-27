package com.github.DaiYuANg.modules.example.application;

import com.github.DaiYuANg.modules.example.application.command.CreateExampleProductCommand;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleCatalogCommandRepository;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleCatalogReadRepository;
import com.github.DaiYuANg.modules.example.application.port.in.ExampleProductCatalogApi;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleProductView;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExampleProductApplicationService implements ExampleProductCatalogApi {

  private final ExampleCatalogCommandRepository catalogCommandRepository;
  private final ExampleCatalogReadRepository catalogReadRepository;

  @Transactional
  public ExampleProductView create(CreateExampleProductCommand command) {
    return catalogCommandRepository.create(command);
  }

  public List<ExampleProductView> listActive() {
    return catalogReadRepository.listActive();
  }
}
