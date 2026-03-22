package com.liangdian.security;

public interface RefreshTokenAuthenticator<T> {
    T authenticate(String refreshToken);
}
