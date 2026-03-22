package com.github.DaiYuANg.security;

public interface TokenIssuer<T> {
    T issue(AuthenticatedUser user);
}
