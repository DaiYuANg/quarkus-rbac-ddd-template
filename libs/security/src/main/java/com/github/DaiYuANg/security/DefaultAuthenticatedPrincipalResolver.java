package com.github.DaiYuANg.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultAuthenticatedPrincipalResolver implements AuthenticatedPrincipalResolver {
    private final TokenContextResolver tokenContextResolver;

    @Override
    public Optional<CurrentAuthenticatedUser> resolve(SecurityIdentity identity) {
        return tokenContextResolver.resolve(identity).map(token -> new CurrentAuthenticatedUser(
            token.subject(),
            token.displayName(),
            token.userType(),
            token.roles(),
            token.permissions(),
            token.attributes()
        ));
    }
}
