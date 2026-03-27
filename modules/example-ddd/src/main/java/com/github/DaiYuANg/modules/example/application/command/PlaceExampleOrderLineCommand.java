package com.github.DaiYuANg.modules.example.application.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PlaceExampleOrderLineCommand(@NotNull Long productId, @Min(1) int quantity) {}
