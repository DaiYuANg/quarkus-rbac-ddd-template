package com.liangdian.security;

public record UsernamePasswordAuthenticationRequest(String username, String password) implements LoginAuthenticationRequest {
    @Override
    public String principal() {
        return username;
    }
}
