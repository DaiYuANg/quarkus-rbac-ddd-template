package com.github.DaiYuANg.mobile.identity;

import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.common.model.Results;
import com.github.DaiYuANg.modules.identity.application.AuthApplicationService;
import com.github.DaiYuANg.modules.identity.application.dto.request.LoginRequest;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponse;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.rest.support.RefreshTokenCookies;
import com.github.DaiYuANg.security.config.AuthSecurityConfig;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.toolkit4j.data.model.envelope.Result;

/** 移动端认证入口：复用 {@link AuthApplicationService}，路径与 Cookie 作用域与 admin-api 隔离，避免进程间串 Cookie。 */
@Path("/api/mobile/v1/auth")
@Produces("application/json")
@Consumes("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MobileAuthResource {

  private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";
  private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
  private static final String REFRESH_COOKIE_PATH = "/api/mobile/v1/auth";

  private final AuthApplicationService authApplicationService;
  private final JsonWebToken jwt;
  private final RefreshTokenStore refreshTokenStore;
  private final AuthSecurityConfig authSecurityConfig;

  @POST
  @Path("/login")
  @PermitAll
  public Response login(@Valid @NonNull LoginRequest req, @Context UriInfo uriInfo) {
    val token = authApplicationService.login(req);
    return Response.ok(Results.ok(token))
        .cookie(
            RefreshTokenCookies.issue(
                REFRESH_COOKIE_PATH,
                REFRESH_TOKEN_COOKIE,
                token.refreshToken(),
                Math.toIntExact(authSecurityConfig.refreshTokenTtlSeconds()),
                isSecureRequest(uriInfo)))
        .build();
  }

  @GET
  @Path("/profile")
  @Authenticated
  public Result<String, UserDetailVo> profile() {
    return Results.ok(authApplicationService.profile(jwt.getName()));
  }

  @GET
  @Path("/me")
  @Authenticated
  public Result<String, MeResponse> me() {
    return Results.ok(authApplicationService.me(jwt.getName()));
  }

  @POST
  @Path("/logout")
  @Authenticated
  public Response logout(
      @HeaderParam(REFRESH_TOKEN_HEADER) String refreshTokenHeader,
      @CookieParam(REFRESH_TOKEN_COOKIE) String refreshTokenCookie,
      @Context UriInfo uriInfo) {
    val refreshToken = resolveRefreshToken(refreshTokenHeader, refreshTokenCookie);
    if (refreshToken == null) {
      throw new BizException(ResultCode.REFRESH_TOKEN_INVALID);
    }
    val owner =
        refreshTokenStore
            .getUsername(refreshToken)
            .orElseThrow(() -> new BizException(ResultCode.REFRESH_TOKEN_INVALID));
    if (!owner.equals(jwt.getName())) {
      throw new BizException(ResultCode.FORBIDDEN);
    }
    authApplicationService.logout(refreshToken);
    return Response.ok(Results.ok())
        .cookie(
            RefreshTokenCookies.cleared(
                REFRESH_COOKIE_PATH, REFRESH_TOKEN_COOKIE, isSecureRequest(uriInfo)))
        .build();
  }

  @POST
  @Path("/auth-refresh")
  @PermitAll
  public Response refresh(
      @HeaderParam(REFRESH_TOKEN_HEADER) String refreshTokenHeader,
      @CookieParam(REFRESH_TOKEN_COOKIE) String refreshTokenCookie,
      @Context UriInfo uriInfo) {
    val refreshToken = resolveRefreshToken(refreshTokenHeader, refreshTokenCookie);
    if (refreshToken == null) {
      throw new BizException(ResultCode.REFRESH_TOKEN_INVALID);
    }
    val token = authApplicationService.refreshToken(refreshToken);
    return Response.ok(Results.ok(token))
        .cookie(
            RefreshTokenCookies.issue(
                REFRESH_COOKIE_PATH,
                REFRESH_TOKEN_COOKIE,
                token.refreshToken(),
                Math.toIntExact(authSecurityConfig.refreshTokenTtlSeconds()),
                isSecureRequest(uriInfo)))
        .build();
  }

  @POST
  @Path("/refresh")
  @PermitAll
  public Response refreshAlias(
      @HeaderParam(REFRESH_TOKEN_HEADER) String refreshTokenHeader,
      @CookieParam(REFRESH_TOKEN_COOKIE) String refreshTokenCookie,
      @Context UriInfo uriInfo) {
    return refresh(refreshTokenHeader, refreshTokenCookie, uriInfo);
  }

  @GET
  @Path("/check/authority")
  @Authenticated
  public Result<String, String> authorityVersion() {
    return Results.ok(authApplicationService.checkAuthorityVersion(jwt.getName()));
  }

  private String resolveRefreshToken(String headerToken, String cookieToken) {
    if (headerToken != null && !headerToken.isBlank()) {
      return headerToken;
    }
    if (cookieToken != null && !cookieToken.isBlank()) {
      return cookieToken;
    }
    return null;
  }

  private boolean isSecureRequest(UriInfo uriInfo) {
    return uriInfo != null
        && uriInfo.getRequestUri() != null
        && "https".equalsIgnoreCase(uriInfo.getRequestUri().getScheme());
  }
}
