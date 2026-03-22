package com.github.DaiYuANg.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RequestMetadataAccess {
    private final Instance<RequestMetadataProvider> provider;

    public RequestMetadata current() {
        if (provider.isUnsatisfied()) {
            return RequestMetadata.empty();
        }
        return provider.get().currentRequest().orElse(RequestMetadata.empty());
    }
}
