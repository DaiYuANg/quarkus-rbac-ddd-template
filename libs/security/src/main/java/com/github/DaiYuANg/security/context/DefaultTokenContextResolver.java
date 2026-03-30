package com.github.DaiYuANg.security.context;

import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import com.github.DaiYuANg.security.token.PrincipalAttributesSerializer;
import com.github.DaiYuANg.security.token.TokenContext;
import com.github.DaiYuANg.security.token.TokenContextBuilder;
import com.github.DaiYuANg.security.token.TokenContextResolver;
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
    return Optional.of(
        TokenContextBuilder.builder()
            .subject(identity.getPrincipal().getName())
            .userType((String) attributes.getOrDefault(PrincipalAttributeKeys.USER_TYPE, "UNKNOWN"))
            .displayName(
                (String)
                    attributes.getOrDefault(
                        PrincipalAttributeKeys.DISPLAY_NAME, identity.getPrincipal().getName()))
            .roles(castSet(attributes.get(PrincipalAttributeKeys.ROLES)))
            .permissions(castSet(attributes.get(PrincipalAttributeKeys.PERMISSIONS)))
            .attributes(attributes)
            .securityIdentity(identity)
            .build());
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
