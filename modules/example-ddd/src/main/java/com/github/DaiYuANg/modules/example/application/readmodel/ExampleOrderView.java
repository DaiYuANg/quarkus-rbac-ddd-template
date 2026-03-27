package com.github.DaiYuANg.modules.example.application.readmodel;

import java.util.List;

public record ExampleOrderView(
    Long id,
    String buyerUsername,
    String status,
    long totalMinor,
    List<ExampleOrderLineView> lines) {}
