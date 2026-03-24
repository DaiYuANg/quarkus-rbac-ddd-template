package com.github.DaiYuANg.security.access;

import com.github.DaiYuANg.security.request.RequestMetadata;
import com.github.DaiYuANg.security.token.TokenContext;
import com.github.DaiYuANg.security.token.TokenContextResolver;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TokenContextAccess {
    private final Instance<SecurityIdentity> securityIdentity;
    private final TokenContextResolver tokenContextResolver;
    private final RequestMetadataAccess requestMetadataAccess;

    public Optional<TokenContext> currentToken() {
        if (securityIdentity.isUnsatisfied()) {
            return Optional.empty();
        }
        return tokenContextResolver.resolve(securityIdentity.get());
    }

    public RequestMetadata requestMetadata() {
        return requestMetadataAccess.current();
    }
}
