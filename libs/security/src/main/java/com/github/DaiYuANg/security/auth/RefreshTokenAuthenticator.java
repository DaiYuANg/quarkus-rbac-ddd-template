package com.github.DaiYuANg.security.auth;

public interface RefreshTokenAuthenticator<T> {
    T authenticate(String refreshToken);
}
