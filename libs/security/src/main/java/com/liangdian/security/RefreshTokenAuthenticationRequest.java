package com.liangdian.security;

public record RefreshTokenAuthenticationRequest(String refreshToken) implements LoginAuthenticationRequest {
    @Override
    public String principal() {
        return refreshToken;
    }
}
