package com.github.DaiYuANg.security.auth;

import com.github.DaiYuANg.security.identity.AuthenticatedUser;

public record AuthenticationResult(AuthenticatedUser user, String providerId) {}
