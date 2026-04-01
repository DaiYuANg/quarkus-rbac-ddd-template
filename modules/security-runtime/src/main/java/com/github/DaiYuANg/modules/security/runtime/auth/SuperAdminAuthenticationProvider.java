package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.cache.PermissionCatalogEntry;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.security.auth.PasswordHasher;
import com.github.DaiYuANg.security.auth.UsernamePasswordAuthenticationRequest;
import com.github.DaiYuANg.security.config.AuthSecurityConfig;
import com.github.DaiYuANg.security.config.SuperAdminAuthorityId;
import com.github.DaiYuANg.security.identity.QuarkusSecurityIdentityFactory;
import com.github.DaiYuANg.security.identity.SecurityPrincipalDefinition;
import com.github.DaiYuANg.security.identity.SecurityPrincipalFactory;
import com.github.DaiYuANg.security.identity.SecurityPrincipalKinds;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class SuperAdminAuthenticationProvider
    implements IdentityProvider<UsernamePasswordAuthenticationRequest> {
  private final AuthSecurityConfig authSecurityConfig;
  private final PasswordHasher passwordHasher;
  private final PermissionCatalogStore permissionCatalogStore;
  private final SecurityPrincipalFactory securityPrincipalFactory;
  private final QuarkusSecurityIdentityFactory securityIdentityFactory;

  @Override
  public int priority() {
    return 100;
  }

  @Override
  public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
    return UsernamePasswordAuthenticationRequest.class;
  }

  @Override
  public Uni<SecurityIdentity> authenticate(
      @NonNull UsernamePasswordAuthenticationRequest request, AuthenticationRequestContext context) {
    return context.runBlocking(() -> authenticateBlocking(request));
  }

  private SecurityIdentity authenticateBlocking(
      @NonNull UsernamePasswordAuthenticationRequest request) {
    val config = authSecurityConfig.superAdmin();
    val configuredUsername = config.username().map(String::trim).orElse("");
    val configuredPasswordHash = config.passwordHash().map(String::trim).orElse("");
    if (configuredUsername.isEmpty() || configuredPasswordHash.isEmpty()) {
      log.atDebug().log("super-admin: account not configured, abstain");
      return null;
    }
    if (!configuredUsername.equalsIgnoreCase(request.username().trim())) {
      log.atDebug()
          .addKeyValue("username", request.username())
          .log("super-admin: user not found, abstain");
      return null;
    }
    if (!passwordHasher.verify(request.password(), configuredPasswordHash)) {
      log.atDebug()
          .addKeyValue("username", request.username())
          .log("super-admin: password mismatch");
      throw new BizException(ResultCode.USERNAME_OR_PASSWORD_INVALID);
    }
    val displayName =
        config.displayName()
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .orElse(configuredUsername);
    val user =
        securityPrincipalFactory.authenticatedUser(
            SecurityPrincipalDefinition.builder()
                .username(configuredUsername)
                .displayName(displayName)
                .userType(SecurityPrincipalKinds.UserType.SUPER_ADMIN)
                .source(SecurityPrincipalKinds.Source.SUPER_ADMIN)
                .providerId(providerId())
                .roles(Set.of(SecurityPrincipalKinds.Role.SUPER_ADMIN))
                .permissions(allPermissionCodes())
                .userId(SuperAdminAuthorityId.forUsername(configuredUsername))
                .build());
    log.atDebug().addKeyValue("username", configuredUsername).log("super-admin: authenticated");
    return securityIdentityFactory.create(user);
  }

  private Set<String> allPermissionCodes() {
    return permissionCatalogStore.getAll().stream()
        .map(PermissionCatalogEntry::code)
        .filter(java.util.Objects::nonNull)
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
  }

  private String providerId() {
    return SecurityPrincipalKinds.Provider.SUPER_ADMIN;
  }
}


