package com.github.DaiYuANg.mobile.identity;

import com.github.DaiYuANg.common.model.Results;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.toolkit4j.data.model.envelope.Result;

@Path("/api/mobile/v1/session")
@Produces("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MobileSessionResource {

  private final JsonWebToken jwt;

  @GET
  @Authenticated
  public Result<String, MobilePrincipalView> currentPrincipal() {
    Object userType = jwt.getClaim(PrincipalAttributeKeys.USER_TYPE);
    Object rolesClaim = jwt.getClaim(PrincipalAttributeKeys.ROLES);
    List<String> roles = new ArrayList<>();
    if (rolesClaim instanceof List<?> list) {
      for (Object o : list) {
        if (o != null) {
          roles.add(String.valueOf(o));
        }
      }
    }
    return Results.ok(
        new MobilePrincipalView(
            jwt.getName(), userType == null ? "" : String.valueOf(userType), List.copyOf(roles)));
  }
}
