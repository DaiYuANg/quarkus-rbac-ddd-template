package com.github.DaiYuANg.accesscontrol.constant;

import org.toolkit4j.data.model.enumeration.EnumValue;

public enum RoleStatus implements EnumValue<Short> {
  DISABLED((short) 0),
  ENABLED((short) 1);

  private final short primaryValue;

  RoleStatus(short primaryValue) {
    this.primaryValue = primaryValue;
  }

  @Override
  public Short getPrimaryValue() {
    return primaryValue;
  }
}
