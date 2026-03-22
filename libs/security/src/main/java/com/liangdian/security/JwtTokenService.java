package com.liangdian.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class JwtTokenService {
    private final AuthSecurityConfig authSecurityConfig;

    public String generate(AuthenticatedUser user, String authorityVersion) {
        return Jwt.subject(user.username())
            .groups(user.permissions())
            .claim("displayName", user.displayName())
            .claim("userType", user.userType())
            .claim("roles", user.roles())
            .claim("permissions", user.permissions())
            .claim("authorityVersion", authorityVersion)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(authSecurityConfig.accessTokenTtlSeconds()))
            .sign();
    }

    public String generateAccessToken(String username, Set<String> permissions) {
        return Jwt.subject(username)
            .groups(permissions)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(authSecurityConfig.accessTokenTtlSeconds()))
            .sign();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public Duration refreshTokenTtl() {
        return Duration.ofSeconds(authSecurityConfig.refreshTokenTtlSeconds());
    }

    public long accessTokenExpiresIn() {
        return authSecurityConfig.refreshTokenTtlSeconds();
    }
}
