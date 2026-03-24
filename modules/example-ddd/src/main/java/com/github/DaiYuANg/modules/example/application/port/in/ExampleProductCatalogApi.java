package com.github.DaiYuANg.modules.example.application.port.in;

import com.github.DaiYuANg.modules.example.application.dto.CreateExampleProductCommand;
import com.github.DaiYuANg.modules.example.application.dto.ExampleProductView;
import java.util.List;

/** Inbound port: example catalog use cases exposed to driving adapters (REST). */
public interface ExampleProductCatalogApi {

  ExampleProductView create(CreateExampleProductCommand command);

  List<ExampleProductView> listActive();
}
