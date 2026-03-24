package com.github.DaiYuANg.api.controller;

import com.github.DaiYuANg.api.dto.response.MeResponse;
import com.github.DaiYuANg.application.auth.AuthApplicationService;
import com.github.DaiYuANg.common.model.Result;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import lombok.RequiredArgsConstructor;

@Path("/api/v1/me")
@Produces("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MeResource {
    private final AuthApplicationService authApplicationService;
    private final SecurityIdentity securityIdentity;

    @GET
    @Authenticated
    public Result<MeResponse> me() {
        return Result.ok(authApplicationService.me(securityIdentity.getPrincipal().getName()));
    }
}
