package com.github.DaiYuANg.modules.example.application.port.driven;

import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrder;

public interface ExampleOrderCommandRepository {

  ExampleOrder save(ExampleOrder order);
}
