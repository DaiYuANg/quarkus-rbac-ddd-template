package com.github.DaiYuANg.security.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthenticationProviders {
  private final AuthenticationProviderRegistry registry;

  public List<LoginAuthenticationProvider<?>> orderedProviders() {
    return registry.orderedProviders();
  }
}
