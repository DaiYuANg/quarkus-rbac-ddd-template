package com.github.DaiYuANg.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequestScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RequestCurrentUserProvider implements CurrentAuthenticatedUserProvider {
    private final SecurityIdentity securityIdentity;
    private final AuthenticatedPrincipalResolver principalResolver;

    @Override
    public Optional<CurrentAuthenticatedUser> getCurrentUser() {
        return principalResolver.resolve(securityIdentity);
    }
}
