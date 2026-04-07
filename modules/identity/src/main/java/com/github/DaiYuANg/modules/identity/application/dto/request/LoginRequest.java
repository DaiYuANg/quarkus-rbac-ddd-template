package com.github.DaiYuANg.modules.identity.application.dto.request;

import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotBlank;

@RecordBuilder
public record LoginRequest(
    @NotBlank(message = "username must not be blank") String username,
    @NotBlank(message = "password must not be blank") String password) {}
