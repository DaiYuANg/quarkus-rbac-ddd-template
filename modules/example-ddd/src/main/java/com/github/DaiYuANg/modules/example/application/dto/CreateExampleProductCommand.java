package com.github.DaiYuANg.modules.example.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateExampleProductCommand(
    @NotBlank @Size(max = 255) String name,
    @Min(0) long priceMinor,
    @Min(0) int stock) {}
