package com.github.DaiYuANg.rest.support;

import jakarta.ws.rs.core.NewCookie;
import lombok.experimental.UtilityClass;

/** Shared HttpOnly refresh-token cookies for any API prefix (admin vs mobile, etc.). */
@UtilityClass
public class RefreshTokenCookies {

  private final String COMMENT = "refresh token";

  public NewCookie issue(
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

  public NewCookie cleared(String cookiePath, String cookieName, boolean secure) {
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
