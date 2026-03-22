package com.github.DaiYuANg.security;

public sealed interface LoginAuthenticationRequest permits UsernamePasswordAuthenticationRequest, RefreshTokenAuthenticationRequest {
    String principal();
}
