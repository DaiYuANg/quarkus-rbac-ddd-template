package com.github.DaiYuANg.persistence.outbox;

import com.github.DaiYuANg.persistence.converter.AbstractShortCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class OutboxStatusConverter extends AbstractShortCodeEnumConverter<OutboxStatus> {
  public OutboxStatusConverter() {
    super(OutboxStatus.class);
  }
}
