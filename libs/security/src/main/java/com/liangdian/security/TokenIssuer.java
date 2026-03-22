package com.liangdian.security;

public interface TokenIssuer<T> {
    T issue(AuthenticatedUser user);
}
