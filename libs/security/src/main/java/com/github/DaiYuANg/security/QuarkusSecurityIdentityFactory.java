package com.github.DaiYuANg.security;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class QuarkusSecurityIdentityFactory {
    private final PrincipalAttributesSerializer principalAttributesSerializer;

    public SecurityIdentity create(AuthenticatedUser user) {
        var builder = QuarkusSecurityIdentity.builder()
            .setPrincipal(user::username)
            .setAnonymous(false);
        user.roles().forEach(builder::addRole);
        user.permissions().forEach(permission -> builder.addPermission(new StringPermission(permission)));
        principalAttributesSerializer.toAttributes(user).forEach(builder::addAttribute);
        return builder.build();
    }

    public SecurityIdentity build(AuthenticatedUser user) {
        return create(user);
    }
}
