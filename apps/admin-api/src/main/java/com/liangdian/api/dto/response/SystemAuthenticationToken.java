package com.liangdian.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SystemAuthenticationToken(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    String authorityVersion
) {}
