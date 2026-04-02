package com.github.DaiYuANg.persistence.converter;

import jakarta.persistence.AttributeConverter;
import org.toolkit4j.data.model.enumeration.EnumValue;
import org.toolkit4j.data.model.enumeration.EnumValues;

public abstract class AbstractShortCodeEnumConverter<E extends Enum<E> & EnumValue<Short>>
    implements AttributeConverter<E, Short> {
  private final Class<E> enumType;

  protected AbstractShortCodeEnumConverter(Class<E> enumType) {
    this.enumType = enumType;
  }

  @Override
  public Short convertToDatabaseColumn(E attribute) {
    return attribute == null ? null : attribute.getPrimaryValue();
  }

  @Override
  public E convertToEntityAttribute(Short dbData) {
    return dbData == null ? null : EnumValues.lookup(enumType).fromPrimaryValue(dbData);
  }
}
