package com.liangdian.security;

public record AuthenticationResult(AuthenticatedUser user, String providerId) {}
