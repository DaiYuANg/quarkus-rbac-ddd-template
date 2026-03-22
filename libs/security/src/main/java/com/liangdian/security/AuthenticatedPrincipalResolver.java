package com.liangdian.security;

import io.quarkus.security.identity.SecurityIdentity;
import java.util.Optional;

public interface AuthenticatedPrincipalResolver {
    Optional<CurrentAuthenticatedUser> resolve(SecurityIdentity identity);
}
