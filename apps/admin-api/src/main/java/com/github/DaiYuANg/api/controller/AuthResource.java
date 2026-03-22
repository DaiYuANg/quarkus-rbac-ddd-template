package com.github.DaiYuANg.api.controller;

import com.github.DaiYuANg.api.dto.request.LoginRequest;
import com.github.DaiYuANg.api.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.api.dto.response.UserDetailVo;
import com.github.DaiYuANg.application.auth.AuthApplicationService;
import com.github.DaiYuANg.common.model.Result;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/v1/auth")
@Produces("application/json")
@Consumes("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthResource {
    private final AuthApplicationService authApplicationService;
    private final JsonWebToken jwt;

    @POST @Path("/login") @PermitAll
    public Result<SystemAuthenticationToken> login(@Valid LoginRequest req) {
        return Result.ok(authApplicationService.login(req));
    }

    @GET @Path("/profile") @Authenticated
    public Result<UserDetailVo> profile() {
        return Result.ok(authApplicationService.profile(jwt.getName()));
    }

    @GET @Path("/me") @Authenticated
    public Result<UserDetailVo> me() {
        return Result.ok(authApplicationService.profile(jwt.getName()));
    }

    @POST @Path("/logout") @PermitAll
    public Result<Void> logout(@HeaderParam("X-Refresh-Token") String refreshToken) {
        authApplicationService.logout(refreshToken);
        return Result.ok();
    }

    @GET @Path("/auth-refresh") @PermitAll
    public Result<SystemAuthenticationToken> refresh(@HeaderParam("X-Refresh-Token") String refreshToken) {
        return Result.ok(authApplicationService.refreshToken(refreshToken));
    }

    @GET @Path("/check/authority") @Authenticated
    public Result<String> authorityVersion() {
        return Result.ok(authApplicationService.checkAuthorityVersion(jwt.getName()));
    }
}
