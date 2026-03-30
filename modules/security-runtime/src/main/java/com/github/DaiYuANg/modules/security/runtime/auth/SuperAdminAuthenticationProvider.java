package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.security.auth.AuthenticationProviderResult;
import com.github.DaiYuANg.security.auth.AuthenticationResult;
import com.github.DaiYuANg.security.auth.LoginAuthenticationProvider;
import com.github.DaiYuANg.security.auth.LoginAuthenticationRequest;
import com.github.DaiYuANg.security.auth.PasswordHasher;
import com.github.DaiYuANg.security.auth.UsernamePasswordAuthenticationRequest;
import com.github.DaiYuANg.security.config.SuperAdminAccountConfig;
import com.github.DaiYuANg.security.config.SuperAdminAuthorityId;
import com.github.DaiYuANg.security.identity.SecurityPrincipalDefinition;
import com.github.DaiYuANg.security.identity.SecurityPrincipalFactory;
import com.github.DaiYuANg.security.identity.SecurityPrincipalKinds;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@ApplicationScoped
@Priority(100)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class SuperAdminAuthenticationProvider
    implements LoginAuthenticationProvider<UsernamePasswordAuthenticationRequest> {
  private final SuperAdminAccountConfig config;
  private final PasswordHasher passwordHasher;
  private final PermissionCatalogStore permissionCatalogStore;
  private final SecurityPrincipalFactory securityPrincipalFactory;

  @Override
  public String providerId() {
    return SecurityPrincipalKinds.Provider.SUPER_ADMIN;
  }

  @Override
  public boolean supports(LoginAuthenticationRequest request) {
    return request instanceof UsernamePasswordAuthenticationRequest;
  }

  @Override
  public AuthenticationProviderResult authenticate(
      @NonNull UsernamePasswordAuthenticationRequest request) {
    val configuredUsername = config.username().map(String::trim).orElse("");
    val configuredPasswordHash = config.passwordHash().map(String::trim).orElse("");
    if (configuredUsername.isEmpty() || configuredPasswordHash.isEmpty()) {
      log.atDebug().log("super-admin: account not configured, abstain");
      return AuthenticationProviderResult.abstain();
    }
    if (!configuredUsername.equalsIgnoreCase(request.username().trim())) {
      log.atDebug()
          .addKeyValue("username", request.username())
          .log("super-admin: user not found, abstain");
      return AuthenticationProviderResult.abstain();
    }
    if (!passwordHasher.verify(request.password(), configuredPasswordHash)) {
      log.atDebug()
          .addKeyValue("username", request.username())
          .log("super-admin: password mismatch");
      return AuthenticationProviderResult.failure(ResultCode.USERNAME_OR_PASSWORD_INVALID);
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
                .providerId(SecurityPrincipalKinds.Provider.SUPER_ADMIN)
                .roles(Set.of(SecurityPrincipalKinds.Role.SUPER_ADMIN))
                .permissions(allPermissionCodes())
                .userId(SuperAdminAuthorityId.forUsername(configuredUsername))
                .build());
    log.atDebug().addKeyValue("username", configuredUsername).log("super-admin: authenticated");
    return AuthenticationProviderResult.success(new AuthenticationResult(user, providerId()));
  }

  private Set<String> allPermissionCodes() {
    return permissionCatalogStore.getAll().stream()
        .map(entry -> entry.code())
        .filter(java.util.Objects::nonNull)
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
  }
}
