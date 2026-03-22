package com.github.DaiYuANg.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class AuthenticationProviderRegistry {
    @Inject Instance<LoginAuthenticationProvider<?>> providers;

    public List<LoginAuthenticationProvider<?>> orderedProviders() {
        return providers.stream()
            .sorted(Comparator.comparingInt((LoginAuthenticationProvider<?> p) -> p.order())
                .thenComparing(LoginAuthenticationProvider::providerId))
            .toList();
    }
}
