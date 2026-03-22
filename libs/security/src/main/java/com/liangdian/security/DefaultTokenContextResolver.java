package com.liangdian.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultTokenContextResolver implements TokenContextResolver {
    private final PrincipalAttributesSerializer serializer;

    @Override
    public Optional<TokenContext> resolve(SecurityIdentity identity) {
        if (identity == null || identity.isAnonymous()) {
            return Optional.empty();
        }
        Map<String, Object> attributes = serializer.deserialize(identity.getAttributes());
        return Optional.of(new TokenContext(
            identity.getPrincipal().getName(),
            (String) attributes.getOrDefault("userType", "UNKNOWN"),
            (String) attributes.getOrDefault("displayName", identity.getPrincipal().getName()),
            castSet(attributes.get("roles")),
            castSet(attributes.get("permissions")),
            attributes,
            identity
        ));
    }

    @SuppressWarnings("unchecked")
    private Set<String> castSet(Object value) {
        if (value instanceof Set<?> set) {
            return (Set<String>) set;
        }
        if (value instanceof Iterable<?> iterable) {
            java.util.LinkedHashSet<String> result = new java.util.LinkedHashSet<>();
            iterable.forEach(item -> result.add(String.valueOf(item)));
            return java.util.Set.copyOf(result);
        }
        return Set.of();
    }
}
