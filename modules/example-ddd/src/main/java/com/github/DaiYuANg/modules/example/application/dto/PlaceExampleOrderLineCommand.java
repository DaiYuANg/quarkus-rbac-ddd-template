package com.github.DaiYuANg.modules.example.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PlaceExampleOrderLineCommand(@NotNull Long productId, @Min(1) int quantity) {}
