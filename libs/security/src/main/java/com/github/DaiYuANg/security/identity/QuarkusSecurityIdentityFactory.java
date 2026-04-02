package com.github.DaiYuANg.security.identity;

import com.github.DaiYuANg.security.token.PrincipalAttributesSerializer;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class QuarkusSecurityIdentityFactory {
  private final PrincipalAttributesSerializer principalAttributesSerializer;

  public SecurityIdentity create(AuthenticatedUser user) {
    val builder =
        QuarkusSecurityIdentity.builder().setPrincipal(user::username).setAnonymous(false);
    user.roles().forEach(builder::addRole);
    user.permissions().forEach(builder::addPermissionAsString);
    principalAttributesSerializer.toAttributes(user).forEach(builder::addAttribute);
    return builder.build();
  }

  public SecurityIdentity build(AuthenticatedUser user) {
    return create(user);
  }
}
