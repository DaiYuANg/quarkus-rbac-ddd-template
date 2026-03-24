package com.github.DaiYuANg.api.controller;

import com.github.DaiYuANg.api.dto.request.LoginRequest;
import com.github.DaiYuANg.api.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.api.dto.response.UserDetailVo;
import com.github.DaiYuANg.application.auth.AuthApplicationService;
import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.common.model.Result;
import com.github.DaiYuANg.security.config.AuthSecurityConfig;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/v1/auth")
@Produces("application/json")
@Consumes("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthResource {
    private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String REFRESH_COOKIE_PATH = "/api/v1/auth";

    private final AuthApplicationService authApplicationService;
    private final JsonWebToken jwt;
    private final RefreshTokenStore refreshTokenStore;
    private final AuthSecurityConfig authSecurityConfig;

    @POST @Path("/login") @PermitAll
    public Response login(@Valid LoginRequest req, @Context UriInfo uriInfo) {
        var token = authApplicationService.login(req);
        return Response.ok(Result.ok(token))
            .cookie(refreshTokenCookie(token.refreshToken(), isSecureRequest(uriInfo)))
            .build();
    }

    @GET @Path("/profile") @Authenticated
    public Result<UserDetailVo> profile() {
        return Result.ok(authApplicationService.profile(jwt.getName()));
    }

    @GET @Path("/me") @Authenticated
    public Result<UserDetailVo> me() {
        return Result.ok(authApplicationService.profile(jwt.getName()));
    }

    @POST @Path("/logout") @Authenticated
    public Response logout(
        @HeaderParam(REFRESH_TOKEN_HEADER) String refreshTokenHeader,
        @CookieParam(REFRESH_TOKEN_COOKIE) String refreshTokenCookie,
        @Context UriInfo uriInfo) {
        var refreshToken = resolveRefreshToken(refreshTokenHeader, refreshTokenCookie);
        if (refreshToken == null) {
            throw new BizException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        var owner = refreshTokenStore.getUsername(refreshToken)
            .orElseThrow(() -> new BizException(ResultCode.REFRESH_TOKEN_INVALID));
        if (!owner.equals(jwt.getName())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        authApplicationService.logout(refreshToken);
        return Response.ok(Result.ok())
            .cookie(clearRefreshTokenCookie(isSecureRequest(uriInfo)))
            .build();
    }

    @POST @Path("/auth-refresh") @PermitAll
    public Response refresh(
        @HeaderParam(REFRESH_TOKEN_HEADER) String refreshTokenHeader,
        @CookieParam(REFRESH_TOKEN_COOKIE) String refreshTokenCookie,
        @Context UriInfo uriInfo) {
        var refreshToken = resolveRefreshToken(refreshTokenHeader, refreshTokenCookie);
        if (refreshToken == null) {
            throw new BizException(ResultCode.REFRESH_TOKEN_INVALID);
        }
        var token = authApplicationService.refreshToken(refreshToken);
        return Response.ok(Result.ok(token))
            .cookie(refreshTokenCookie(token.refreshToken(), isSecureRequest(uriInfo)))
            .build();
    }

    @GET @Path("/check/authority") @Authenticated
    public Result<String> authorityVersion() {
        return Result.ok(authApplicationService.checkAuthorityVersion(jwt.getName()));
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

    private NewCookie refreshTokenCookie(String refreshToken, boolean secure) {
        return new NewCookie(
            REFRESH_TOKEN_COOKIE,
            refreshToken,
            REFRESH_COOKIE_PATH,
            null,
            NewCookie.DEFAULT_VERSION,
            "refresh token",
            Math.toIntExact(authSecurityConfig.refreshTokenTtlSeconds()),
            null,
            secure,
            true
        );
    }

    private NewCookie clearRefreshTokenCookie(boolean secure) {
        return new NewCookie(
            REFRESH_TOKEN_COOKIE,
            "",
            REFRESH_COOKIE_PATH,
            null,
            NewCookie.DEFAULT_VERSION,
            "refresh token",
            0,
            null,
            secure,
            true
        );
    }

    private boolean isSecureRequest(UriInfo uriInfo) {
        return uriInfo != null
            && uriInfo.getRequestUri() != null
            && "https".equalsIgnoreCase(uriInfo.getRequestUri().getScheme());
    }
}
