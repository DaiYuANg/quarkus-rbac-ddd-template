package com.github.DaiYuANg.persistence.query;

public enum SortDirection {
  ASC,
  DESC;

  public static SortDirection fromNullable(String value, SortDirection fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return "asc".equalsIgnoreCase(value) ? ASC : DESC;
  }
}
