package com.github.DaiYuANg.rest.support;

import jakarta.ws.rs.core.NewCookie;

/** Shared HttpOnly refresh-token cookies for any API prefix (admin vs mobile, etc.). */
public final class RefreshTokenCookies {

  private static final String COMMENT = "refresh token";

  private RefreshTokenCookies() {}

  public static NewCookie issue(
      String cookiePath, String cookieName, String token, int maxAgeSeconds, boolean secure) {
    return new NewCookie.Builder(cookieName)
        .value(token)
        .path(cookiePath)
        .comment(COMMENT)
        .maxAge(maxAgeSeconds)
        .secure(secure)
        .httpOnly(true)
        .build();
  }

  public static NewCookie cleared(String cookiePath, String cookieName, boolean secure) {
    return new NewCookie.Builder(cookieName)
        .value("")
        .path(cookiePath)
        .comment(COMMENT)
        .maxAge(0)
        .secure(secure)
        .httpOnly(true)
        .build();
  }
}
