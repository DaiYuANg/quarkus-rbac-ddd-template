package com.github.DaiYuANg.mobile.identity;

import com.github.DaiYuANg.common.model.Result;
import com.github.DaiYuANg.modules.identity.application.AuthApplicationService;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponse;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import lombok.RequiredArgsConstructor;

@Path("/api/mobile/v1/me")
@Produces("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MobileMeResource {

  private final AuthApplicationService authApplicationService;
  private final SecurityIdentity securityIdentity;

  @GET
  @Authenticated
  public Result<MeResponse> me() {
    return Result.ok(authApplicationService.me(securityIdentity.getPrincipal().getName()));
  }
}
