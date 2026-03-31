package com.github.DaiYuANg.security.access;

import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import com.github.DaiYuANg.security.token.TokenContextResolver;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CurrentUserAccess {
  private final Instance<SecurityIdentity> securityIdentity;
  private final TokenContextResolver tokenContextResolver;

  public Optional<CurrentAuthenticatedUser> currentUser() {
    if (securityIdentity.isUnsatisfied()) {
      return Optional.empty();
    }
    try {
      return tokenContextResolver
          .resolve(securityIdentity.get())
          .map(
              token ->
                  new CurrentAuthenticatedUser(
                      token.subject(),
                      token.displayName(),
                      token.userType(),
                      token.roles(),
                      token.permissions(),
                      token.attributes()));
    } catch (ContextNotActiveException ignored) {
      return Optional.empty();
    }
  }
}
