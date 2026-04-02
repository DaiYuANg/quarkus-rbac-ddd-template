package com.github.DaiYuANg.persistence.outbox;

import org.toolkit4j.data.model.enumeration.EnumValue;

public enum OutboxStatus implements EnumValue<Short> {
  PENDING((short) 0),
  PUBLISHED((short) 1),
  FAILED((short) 2);

  private final short primaryValue;

  OutboxStatus(short primaryValue) {
    this.primaryValue = primaryValue;
  }

  @Override
  public Short getPrimaryValue() {
    return primaryValue;
  }
}
