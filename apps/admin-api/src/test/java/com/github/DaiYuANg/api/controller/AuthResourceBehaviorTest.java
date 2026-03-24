package com.github.DaiYuANg.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.api.dto.request.LoginRequest;
import com.github.DaiYuANg.api.dto.response.MeResponse;
import com.github.DaiYuANg.api.dto.response.MeRoleItem;
import com.github.DaiYuANg.api.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.application.auth.AuthApplicationService;
import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.security.config.AuthSecurityConfig;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

class AuthResourceBehaviorTest {

  @Test
  void loginSetsHttpOnlyRefreshTokenCookie() {
    var authApplicationService = mock(AuthApplicationService.class);
    var jwt = mock(JsonWebToken.class);
    var refreshTokenStore = mock(RefreshTokenStore.class);
    var authSecurityConfig = mock(AuthSecurityConfig.class);

    when(authSecurityConfig.refreshTokenTtlSeconds()).thenReturn(3600L);
    when(authApplicationService.login(any()))
        .thenReturn(new SystemAuthenticationToken("access", "rt-login", "Bearer", 120L, "v1"));

    var resource =
        new AuthResource(authApplicationService, jwt, refreshTokenStore, authSecurityConfig);

    Response response = resource.login(new LoginRequest("root", "root"), uriInfo("https://a.test"));

    assertEquals(200, response.getStatus());
    NewCookie cookie = response.getCookies().get("refresh_token");
    assertNotNull(cookie);
    assertEquals("rt-login", cookie.getValue());
    assertEquals(true, cookie.isHttpOnly());
    assertEquals(true, cookie.isSecure());
    assertEquals("/api/v1/auth", cookie.getPath());
    assertEquals(3600, cookie.getMaxAge());
  }

  @Test
  void refreshUsesHeaderTokenBeforeCookieToken() {
    var authApplicationService = mock(AuthApplicationService.class);
    var jwt = mock(JsonWebToken.class);
    var refreshTokenStore = mock(RefreshTokenStore.class);
    var authSecurityConfig = mock(AuthSecurityConfig.class);
    when(authSecurityConfig.refreshTokenTtlSeconds()).thenReturn(3600L);
    when(authApplicationService.refreshToken("header-token"))
        .thenReturn(new SystemAuthenticationToken("access-new", "rt-new", "Bearer", 120L, "v2"));

    var resource =
        new AuthResource(authApplicationService, jwt, refreshTokenStore, authSecurityConfig);

    Response response =
        resource.refresh("header-token", "cookie-token", uriInfo("https://a.test"));

    assertEquals(200, response.getStatus());
    verify(authApplicationService).refreshToken("header-token");
    NewCookie cookie = response.getCookies().get("refresh_token");
    assertNotNull(cookie);
    assertEquals("rt-new", cookie.getValue());
  }

  @Test
  void refreshFallsBackToCookieTokenWhenHeaderMissing() {
    var authApplicationService = mock(AuthApplicationService.class);
    var jwt = mock(JsonWebToken.class);
    var refreshTokenStore = mock(RefreshTokenStore.class);
    var authSecurityConfig = mock(AuthSecurityConfig.class);
    when(authSecurityConfig.refreshTokenTtlSeconds()).thenReturn(3600L);
    when(authApplicationService.refreshToken("cookie-token"))
        .thenReturn(new SystemAuthenticationToken("access-new", "rt-new", "Bearer", 120L, "v2"));

    var resource =
        new AuthResource(authApplicationService, jwt, refreshTokenStore, authSecurityConfig);

    resource.refresh(null, "cookie-token", uriInfo("https://a.test"));

    verify(authApplicationService).refreshToken("cookie-token");
  }

  @Test
  void logoutRejectsRefreshTokenOwnedByAnotherUser() {
    var authApplicationService = mock(AuthApplicationService.class);
    var jwt = mock(JsonWebToken.class);
    var refreshTokenStore = mock(RefreshTokenStore.class);
    var authSecurityConfig = mock(AuthSecurityConfig.class);

    when(jwt.getName()).thenReturn("alice");
    when(refreshTokenStore.getUsername("rt-1")).thenReturn(Optional.of("bob"));

    var resource =
        new AuthResource(authApplicationService, jwt, refreshTokenStore, authSecurityConfig);

    var ex =
        assertThrows(
            BizException.class,
            () -> resource.logout("rt-1", null, uriInfo("https://a.test")));
    assertEquals(ResultCode.FORBIDDEN, ex.getResultCode());
  }

  @Test
  void meUsesAuthApplicationServiceMeContract() {
    var authApplicationService = mock(AuthApplicationService.class);
    var jwt = mock(JsonWebToken.class);
    var refreshTokenStore = mock(RefreshTokenStore.class);
    var authSecurityConfig = mock(AuthSecurityConfig.class);
    when(jwt.getName()).thenReturn("root");
    var me =
        new MeResponse(
            "1", "Root", "root@example.com", List.of(new MeRoleItem("1", "admin")), Set.of("user:view"));
    when(authApplicationService.me("root")).thenReturn(me);

    var resource =
        new AuthResource(authApplicationService, jwt, refreshTokenStore, authSecurityConfig);

    var response = resource.me();
    assertEquals("1", response.data().id());
    assertEquals("Root", response.data().name());
    assertEquals(1, response.data().roles().size());
  }

  private UriInfo uriInfo(String rawUri) {
    var uriInfo = mock(UriInfo.class);
    when(uriInfo.getRequestUri()).thenReturn(URI.create(rawUri));
    return uriInfo;
  }
}
