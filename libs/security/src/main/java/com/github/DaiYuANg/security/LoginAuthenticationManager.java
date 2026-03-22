package com.github.DaiYuANg.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LoginAuthenticationManager {
    private final AuthenticationProviders authenticationProviders;
    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler failureHandler;

    public AuthenticationResult authenticate(LoginAuthenticationRequest request) {
        AuthenticationProviderResult lastFailure = null;
        for (var provider : authenticationProviders.orderedProviders()) {
            if (!provider.supports(request)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            var casted = (LoginAuthenticationProvider<LoginAuthenticationRequest>) provider;
            var result = casted.authenticate(request);
            if (result == null || result.decision() == AuthenticationDecision.ABSTAIN) {
                continue;
            }
            if (result.decision() == AuthenticationDecision.FAILURE) {
                lastFailure = result;
                continue;
            }
            return successHandler.onSuccess(request, result.authentication());
        }
        if (lastFailure != null) {
            throw failureHandler.toException(request, lastFailure);
        }
        throw failureHandler.unsupported(request);
    }
}
