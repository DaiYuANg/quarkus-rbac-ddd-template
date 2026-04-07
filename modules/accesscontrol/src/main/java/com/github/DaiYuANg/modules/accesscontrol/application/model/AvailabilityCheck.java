package com.github.DaiYuANg.modules.accesscontrol.application.model;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Objects;
import java.util.function.Consumer;

@RecordBuilder
public record AvailabilityCheck(String next, String current, Consumer<String> validator) {
  public boolean changed() {
    return next != null && !Objects.equals(next, current);
  }

  public void validate() {
    validator.accept(next);
  }
}
