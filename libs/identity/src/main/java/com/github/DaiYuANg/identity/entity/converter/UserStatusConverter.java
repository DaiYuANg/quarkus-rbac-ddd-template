package com.github.DaiYuANg.identity.entity.converter;

import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.persistence.converter.AbstractShortCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class UserStatusConverter extends AbstractShortCodeEnumConverter<UserStatus> {
  public UserStatusConverter() {
    super(UserStatus.class);
  }
}
