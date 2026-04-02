package com.github.DaiYuANg.accesscontrol.entity.converter;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import com.github.DaiYuANg.persistence.converter.AbstractShortCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoleStatusConverter extends AbstractShortCodeEnumConverter<RoleStatus> {
  public RoleStatusConverter() {
    super(RoleStatus.class);
  }
}
