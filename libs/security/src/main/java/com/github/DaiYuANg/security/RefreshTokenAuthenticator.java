package com.github.DaiYuANg.security;

public interface RefreshTokenAuthenticator<T> {
    T authenticate(String refreshToken);
}
