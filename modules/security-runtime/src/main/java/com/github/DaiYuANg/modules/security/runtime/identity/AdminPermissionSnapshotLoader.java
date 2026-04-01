package com.github.DaiYuANg.modules.security.runtime.identity;

import com.github.DaiYuANg.cache.PermissionCatalogEntry;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.security.config.AuthSecurityConfig;
import com.github.DaiYuANg.security.config.SuperAdminAuthorityId;
import com.github.DaiYuANg.security.config.SuperAdminAuthorityVersion;
import com.github.DaiYuANg.security.identity.SecurityPrincipalDefinition;
import com.github.DaiYuANg.security.identity.SecurityPrincipalFactory;
import com.github.DaiYuANg.security.identity.SecurityPrincipalKinds;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotLoader;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * Loads a {@link PermissionSnapshot} for a principal.
 *
 * <p>DB users: resolve roles/permissions via repository-level typed queries to avoid initializing
 * the RBAC entity graph (prevents N+1 lazy loads).
 *
 * <p>Super admin: resolve from {@code app.security.super-admin}. It always receives the full
 * permission catalog and a stable synthetic negative {@code userId} so Valkey storage shares the
 * same layout as DB users.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminPermissionSnapshotLoader implements PermissionSnapshotLoader {
  private final UserRepository userRepository;
  private final PermissionCatalogStore permissionCatalogStore;
  private final AuthSecurityConfig authSecurityConfig;
  private final SecurityPrincipalFactory securityPrincipalFactory;
  private final AdminDbUserAuthoritySupport userAuthoritySupport;

  @Override
  @Transactional
  public Optional<PermissionSnapshot> load(@NonNull String username) {
    val user = userRepository.findByUsername(username).orElse(null);
    if (user != null) {
      return snapshotFromDbUser(user);
    }
    return snapshotFromSuperAdmin(username);
  }

  @Override
  @Transactional
  public Optional<PermissionSnapshot> load(Long userId, String usernameHint) {
    if (userId != null && userId > 0) {
      return userRepository.findByIdOptional(userId).flatMap(this::snapshotFromDbUser);
    }
    val normalizedUsername = normalize(usernameHint);
    if (normalizedUsername == null) {
      return Optional.empty();
    }
    return load(normalizedUsername)
        .filter(snapshot -> userId == null || Objects.equals(userId, snapshot.userId()));
  }

  private Optional<PermissionSnapshot> snapshotFromDbUser(@NonNull SysUser user) {
    if (user.userStatus != UserStatus.ENABLED) {
      return Optional.empty();
    }
    return Optional.of(userAuthoritySupport.permissionSnapshot(user));
  }

  private Optional<PermissionSnapshot> snapshotFromSuperAdmin(@NonNull String username) {
    val superAdminAccountConfig = authSecurityConfig.superAdmin();
    val configuredUsername = normalize(superAdminAccountConfig.username().orElse(null));
    val normalizedUsername = normalize(username);
    if (configuredUsername == null
        || normalizedUsername == null
        || !configuredUsername.equalsIgnoreCase(normalizedUsername)) {
      return Optional.empty();
    }
    val permissions =
        permissionCatalogStore.getAll().stream()
            .map(PermissionCatalogEntry::code)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(code -> !code.isEmpty())
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    val displayName =
        superAdminAccountConfig
            .displayName()
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .orElse(configuredUsername);
    val principal =
        securityPrincipalFactory.authenticatedUser(
            SecurityPrincipalDefinition.builder()
                .username(configuredUsername)
                .displayName(displayName)
                .userType(SecurityPrincipalKinds.UserType.SUPER_ADMIN)
                .source(SecurityPrincipalKinds.Source.SUPER_ADMIN)
                .providerId(SecurityPrincipalKinds.Provider.SUPER_ADMIN)
                .roles(Set.of(SecurityPrincipalKinds.Role.SUPER_ADMIN))
                .permissions(permissions)
                .userId(SuperAdminAuthorityId.forUsername(configuredUsername))
                .build());
    return Optional.of(
        securityPrincipalFactory.snapshot(principal, SuperAdminAuthorityVersion.VALUE));
  }

  // DB permissions/roles are resolved via UserRepository queries to avoid N+1 lazy loads.

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
