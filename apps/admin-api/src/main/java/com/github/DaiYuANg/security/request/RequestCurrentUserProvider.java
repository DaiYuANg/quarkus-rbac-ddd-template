package com.github.DaiYuANg.security.request;

import com.github.DaiYuANg.security.identity.AuthenticatedPrincipalResolver;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUserProvider;
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
