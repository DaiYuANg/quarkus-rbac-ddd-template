package com.github.DaiYuANg.security.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LoginAuthenticationManager {
  private static final Logger log = LoggerFactory.getLogger(LoginAuthenticationManager.class);
  private final AuthenticationProviders authenticationProviders;
  private final AuthenticationSuccessHandler successHandler;
  private final AuthenticationFailureHandler failureHandler;

  public AuthenticationResult authenticate(@NonNull LoginAuthenticationRequest request) {
    log.atDebug().addKeyValue("requestType", request.getClass().getSimpleName()).log("authenticate");
    AuthenticationProviderResult lastFailure = null;
    for (var provider : authenticationProviders.orderedProviders()) {
      if (!provider.supports(request)) {
        log.debug("provider {} does not support request, skip", provider.providerId());
        continue;
      }
      @SuppressWarnings("unchecked")
      var casted = (LoginAuthenticationProvider<LoginAuthenticationRequest>) provider;
      var result = casted.authenticate(request);
      if (result == null || result.decision() == AuthenticationDecision.ABSTAIN) {
        log.atDebug().addKeyValue("provider", provider.providerId()).log("provider abstained");
        continue;
      }
      if (result.decision() == AuthenticationDecision.FAILURE) {
        log.atDebug().addKeyValue("provider", provider.providerId()).log("provider failed");
        lastFailure = result;
        continue;
      }
      log.atDebug().addKeyValue("provider", provider.providerId()).log("provider succeeded");
      return successHandler.onSuccess(request, result.authentication());
    }
    if (lastFailure != null) {
      throw failureHandler.toException(request, lastFailure);
    }
    throw failureHandler.unsupported(request);
  }
}
