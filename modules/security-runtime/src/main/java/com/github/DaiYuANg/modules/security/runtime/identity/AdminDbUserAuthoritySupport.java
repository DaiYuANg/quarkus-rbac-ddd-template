package com.github.DaiYuANg.modules.security.runtime.identity;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.security.config.IdentityPrincipalConfig;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.SecurityPrincipalDefinition;
import com.github.DaiYuANg.security.identity.SecurityPrincipalFactory;
import com.github.DaiYuANg.security.identity.SecurityPrincipalKinds;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminDbUserAuthoritySupport {
  private final UserRepository userRepository;
  private final AuthorityVersionStore authorityVersionStore;
  private final IdentityPrincipalConfig identityPrincipalConfig;
  private final SecurityPrincipalFactory securityPrincipalFactory;

  public AuthenticatedUser authenticatedUser(@NonNull SysUser user) {
    val roles = new LinkedHashSet<>(userRepository.findRoleCodesByUsername(user.username));
    val permissions = new LinkedHashSet<>(userRepository.findPermissionCodesByUsername(user.username));
    return securityPrincipalFactory.authenticatedUser(
        SecurityPrincipalDefinition.builder()
            .username(user.username)
            .displayName(user.nickname)
            .userType(identityPrincipalConfig.dbUserType())
            .source(SecurityPrincipalKinds.Source.DB)
            .providerId(SecurityPrincipalKinds.Provider.DB_USER)
            .roles(roles)
            .permissions(permissions)
            .userId(user.id)
            .build());
  }

  public PermissionSnapshot permissionSnapshot(@NonNull SysUser user) {
    return securityPrincipalFactory.snapshot(
        authenticatedUser(user), authorityVersionStore.versionFor(user.username));
  }
}
