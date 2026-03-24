package com.github.DaiYuANg.security.auth;

public record UsernamePasswordAuthenticationRequest(String username, String password) implements LoginAuthenticationRequest {
    @Override
    public String principal() {
        return username;
    }
}
