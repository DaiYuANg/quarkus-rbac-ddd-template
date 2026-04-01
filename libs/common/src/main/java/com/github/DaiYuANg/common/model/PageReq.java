package com.github.DaiYuANg.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.val;
import org.toolkit4j.data.model.page.PageRequest;

import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
public class PageReq extends PageRequest {
  private String keyword;

  private String sortBy;

  private String sortDirection;

  protected final boolean isAscending(boolean defaultAsc) {
    return normalizedValue(getSortDirection())
      .map("asc"::equalsIgnoreCase)
      .orElse(defaultAsc);
  }

  protected final Optional<String> normalizedValue(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    val trimmed = value.trim();
    return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
  }

  protected final Optional<String> resolvedSortBy(Map<String, String> aliases) {
    return normalizedValue(getSortBy()).map(sortBy -> aliases.getOrDefault(sortBy, sortBy));
  }
}
