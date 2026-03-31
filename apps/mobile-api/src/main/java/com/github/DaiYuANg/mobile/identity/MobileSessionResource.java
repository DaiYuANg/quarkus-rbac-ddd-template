package com.github.DaiYuANg.mobile.identity;

import com.github.DaiYuANg.common.model.Results;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import com.google.common.base.Strings;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import java.util.List;
import java.util.Objects;
import lombok.val;
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
    val userType = jwt.getClaim(PrincipalAttributeKeys.USER_TYPE);
    val rolesClaim = jwt.getClaim(PrincipalAttributeKeys.ROLES);
    val roles =
        rolesClaim instanceof List<?> list
            ? list.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList()
            : List.<String>of();
    return Results.ok(
        new MobilePrincipalView(
            jwt.getName(), Strings.nullToEmpty(Objects.toString(userType, null)), List.copyOf(roles)));
  }
}
