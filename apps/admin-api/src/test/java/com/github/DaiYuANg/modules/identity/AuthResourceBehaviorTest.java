package com.github.DaiYuANg.modules.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.modules.identity.application.AuthApplicationService;
import com.github.DaiYuANg.modules.identity.application.dto.request.LoginRequest;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponse;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponseBuilder;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeRoleItem;
import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationTokenBuilder;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.User;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

class AuthResourceBehaviorTest {

  @Test
  void loginSetsHttpOnlyRefreshTokenCookie() {
    var authApplicationService = mock(AuthApplicationService.class);
    var jwt = mock(JsonWebToken.class);

    when(authApplicationService.refreshTokenTtlSeconds()).thenReturn(3600L);
    when(authApplicationService.login(any()))
        .thenReturn(
            SystemAuthenticationTokenBuilder.builder()
                .accessToken("access")
                .refreshToken("rt-login")
                .tokenType("Bearer")
                .expiresIn(120L)
                .authorityVersion("v1")
                .build());

    var resource = new AuthResource(authApplicationService, jwt);

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
    when(authApplicationService.refreshTokenTtlSeconds()).thenReturn(3600L);
    when(authApplicationService.refreshToken("header-token"))
        .thenReturn(
            SystemAuthenticationTokenBuilder.builder()
                .accessToken("access-new")
                .refreshToken("rt-new")
                .tokenType("Bearer")
                .expiresIn(120L)
                .authorityVersion("v2")
                .build());

    var resource = new AuthResource(authApplicationService, jwt);

    Response response = resource.refresh("header-token", "cookie-token", uriInfo("https://a.test"));

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
    when(authApplicationService.refreshTokenTtlSeconds()).thenReturn(3600L);
    when(authApplicationService.refreshToken("cookie-token"))
        .thenReturn(
            SystemAuthenticationTokenBuilder.builder()
                .accessToken("access-new")
                .refreshToken("rt-new")
                .tokenType("Bearer")
                .expiresIn(120L)
                .authorityVersion("v2")
                .build());

    var resource = new AuthResource(authApplicationService, jwt);

    resource.refresh(null, "cookie-token", uriInfo("https://a.test"));

    verify(authApplicationService).refreshToken("cookie-token");
  }

  @Test
  void logoutDelegatesCurrentUserAndRefreshTokenToApplicationService() {
    var authApplicationService = mock(AuthApplicationService.class);
    var jwt = mock(JsonWebToken.class);

    when(jwt.getName()).thenReturn("alice");

    var resource = new AuthResource(authApplicationService, jwt);

    resource.logout("rt-1", null, uriInfo("https://a.test"));

    verify(authApplicationService).logout("alice", "rt-1");
  }

  @Test
  void logoutPropagatesForbiddenFromApplicationService() {
    var authApplicationService = mock(AuthApplicationService.class);
    var jwt = mock(JsonWebToken.class);

    when(jwt.getName()).thenReturn("alice");
    doThrow(new BizException(ResultCode.FORBIDDEN))
        .when(authApplicationService)
        .logout("alice", "rt-1");

    var resource = new AuthResource(authApplicationService, jwt);

    var ex =
        assertThrows(
            BizException.class, () -> resource.logout("rt-1", null, uriInfo("https://a.test")));
    assertEquals(ResultCode.FORBIDDEN, ex.getResultCode());
  }

  @Test
  void meUsesAuthApplicationServiceMeContract() {
    var authApplicationService = mock(AuthApplicationService.class);
    var jwt = mock(JsonWebToken.class);
    when(jwt.getName()).thenReturn("root");
    var me =
        MeResponseBuilder.builder()
            .id("1")
            .name("Root")
            .email("root@example.com")
            .roles(List.of(new MeRoleItem("1", "admin")))
            .permissions(Set.of(User.VIEW))
            .build();
    when(authApplicationService.me("root")).thenReturn(me);

    var resource = new AuthResource(authApplicationService, jwt);

    var response = resource.me();
    assertEquals("1", response.getData().id());
    assertEquals("Root", response.getData().name());
    assertEquals(1, response.getData().roles().size());
  }

  private UriInfo uriInfo(String rawUri) {
    var uriInfo = mock(UriInfo.class);
    when(uriInfo.getRequestUri()).thenReturn(URI.create(rawUri));
    return uriInfo;
  }
}
