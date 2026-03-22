package com.liangdian.security;

public sealed interface LoginAuthenticationRequest permits UsernamePasswordAuthenticationRequest, RefreshTokenAuthenticationRequest {
    String principal();
}
