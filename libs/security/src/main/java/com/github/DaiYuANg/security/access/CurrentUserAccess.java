package com.github.DaiYuANg.security.access;

import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUserProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CurrentUserAccess {
  private final Instance<CurrentAuthenticatedUserProvider> provider;

  public Optional<CurrentAuthenticatedUser> currentUser() {
    if (provider.isUnsatisfied()) {
      return Optional.empty();
    }
    return provider.get().getCurrentUser();
  }
}
