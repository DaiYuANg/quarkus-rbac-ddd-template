package com.github.DaiYuANg.security.auth;

public sealed interface LoginAuthenticationRequest permits UsernamePasswordAuthenticationRequest, RefreshTokenAuthenticationRequest {
    String principal();
}
