package com.github.DaiYuANg.persistence.query;

import java.util.List;

public record PageSlice<T>(List<T> content, long total) {
  public PageSlice {
    content = content == null ? List.of() : List.copyOf(content);
  }
}
