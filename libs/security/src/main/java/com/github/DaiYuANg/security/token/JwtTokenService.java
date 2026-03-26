package com.github.DaiYuANg.security.token;

import com.github.DaiYuANg.security.config.AuthSecurityConfig;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Issues JWTs for authenticated principals.
 *
 * <p>Claims use {@link com.github.DaiYuANg.security.identity.PrincipalAttributeKeys} to guarantee
 * key consistency across:
 *
 * <ul>
 *   <li>JWT custom claims
 *   <li>{@code SecurityIdentity} attributes
 *   <li>Valkey snapshots and attribute serialization
 * </ul>
 *
 * <p>Permissions are still put into {@code groups} for compatibility with Quarkus' endpoint-level
 * permission checks, while the canonical permission set is enforced by snapshot augmentation.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor
public class JwtTokenService {
  private final AuthSecurityConfig authSecurityConfig;

  public String generate(AuthenticatedUser user, String authorityVersion) {
    var builder =
        Jwt.subject(user.username())
            .groups(user.permissions())
            .claim(PrincipalAttributeKeys.DISPLAY_NAME, user.displayName())
            .claim(PrincipalAttributeKeys.USER_TYPE, user.userType())
            .claim(PrincipalAttributeKeys.ROLES, user.roles())
            .claim(PrincipalAttributeKeys.PERMISSIONS, user.permissions())
            .claim(PrincipalAttributeKeys.AUTHORITY_VERSION, authorityVersion);
    if (user.userId() != null) {
      builder.claim(PrincipalAttributeKeys.USER_ID, user.userId());
    }
    return builder
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
    return authSecurityConfig.accessTokenTtlSeconds();
  }
}
