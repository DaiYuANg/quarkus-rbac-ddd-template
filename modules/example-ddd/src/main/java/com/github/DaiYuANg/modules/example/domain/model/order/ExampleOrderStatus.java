package com.github.DaiYuANg.modules.example.domain.model.order;

import org.toolkit4j.data.model.enumeration.EnumValue;

public enum ExampleOrderStatus implements EnumValue<Short> {
  CREATED((short) 0);

  private final short primaryValue;

  ExampleOrderStatus(short primaryValue) {
    this.primaryValue = primaryValue;
  }

  @Override
  public Short getPrimaryValue() {
    return primaryValue;
  }
}
