package com.github.DaiYuANg.modules.example.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PlaceExampleOrderCommand(@NotEmpty List<@Valid PlaceExampleOrderLineCommand> lines) {}
