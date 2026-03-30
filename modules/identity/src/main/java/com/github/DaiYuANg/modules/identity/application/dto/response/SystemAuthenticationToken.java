package com.github.DaiYuANg.modules.identity.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.soabase.recordbuilder.core.RecordBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@RecordBuilder
public record SystemAuthenticationToken(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    String authorityVersion) {}
