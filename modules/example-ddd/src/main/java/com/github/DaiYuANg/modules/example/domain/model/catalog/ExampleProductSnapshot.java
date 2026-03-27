package com.github.DaiYuANg.modules.example.domain.model.catalog;

public record ExampleProductSnapshot(
    Long id, String name, long priceMinor, int stock, boolean active) {}
