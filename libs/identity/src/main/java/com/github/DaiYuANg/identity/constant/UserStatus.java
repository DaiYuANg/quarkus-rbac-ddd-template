package com.github.DaiYuANg.identity.constant;

import org.toolkit4j.data.model.enumeration.EnumValue;

public enum UserStatus implements EnumValue<Short> {
  DISABLED((short) 0),
  ENABLED((short) 1);

  private final short primaryValue;

  UserStatus(short primaryValue) {
    this.primaryValue = primaryValue;
  }

  @Override
  public Short getPrimaryValue() {
    return primaryValue;
  }
}
