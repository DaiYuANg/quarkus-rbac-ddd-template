package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrderStatus;
import com.github.DaiYuANg.persistence.converter.AbstractShortCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ExampleOrderStatusConverter
    extends AbstractShortCodeEnumConverter<ExampleOrderStatus> {
  public ExampleOrderStatusConverter() {
    super(ExampleOrderStatus.class);
  }
}
