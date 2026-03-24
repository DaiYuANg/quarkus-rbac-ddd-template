package com.github.DaiYuANg.modules.example.application.dto;

import java.util.List;

public record ExampleOrderView(
    Long id,
    String buyerUsername,
    String status,
    long totalMinor,
    List<ExampleOrderLineView> lines) {}
