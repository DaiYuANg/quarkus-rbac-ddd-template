package com.github.DaiYuANg.mobile.identity;

import com.github.DaiYuANg.common.model.Results;
import com.github.DaiYuANg.modules.identity.application.AuthApplicationService;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponse;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import lombok.RequiredArgsConstructor;
import org.toolkit4j.data.model.envelope.Result;

@Path("/api/mobile/v1/me")
@Produces("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MobileMeResource {

  private final AuthApplicationService authApplicationService;
  private final SecurityIdentity securityIdentity;

  @GET
  @Authenticated
  public Result<String, MeResponse> me() {
    return Results.ok(authApplicationService.me(securityIdentity.getPrincipal().getName()));
  }
}
