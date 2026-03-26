package com.github.DaiYuANg.modules.security.runtime.identity;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.security.config.ConfigUserAccountConfig;
import com.github.DaiYuANg.security.config.ConfigUserAccounts;
import com.github.DaiYuANg.security.config.ConfigUserAuthorityId;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotLoader;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Loads a {@link PermissionSnapshot} for a principal.
 *
 * <p>DB users: resolve roles/permissions via repository-level typed queries to avoid initializing
 * the RBAC entity graph (prevents N+1 lazy loads).
 *
 * <p>Config users: resolve from {@code app.security.config-users} and assign a stable synthetic
 * negative {@code userId} so Valkey storage shares the same layout as DB users.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminPermissionSnapshotLoader implements PermissionSnapshotLoader {
  private final UserRepository userRepository;
  private final AuthorityVersionStore authorityVersionStore;
  private final ConfigUserAccountConfig configUserAccountConfig;

  @ConfigProperty(name = "app.identity.db-user-type", defaultValue = "ADMIN")
  String dbUserType;

  @ConfigProperty(name = "app.identity.config-user-fallback-type", defaultValue = "CONFIG")
  String configUserFallbackType;

  @Override
  @Transactional
  public Optional<PermissionSnapshot> load(String username) {
    var user = userRepository.findByUsername(username).orElse(null);
    if (user != null) {
      return snapshotFromDbUser(user);
    }
    return ConfigUserAccounts.find(configUserAccountConfig, username)
        .map(entry -> snapshotFromConfig(entry, username));
  }

  @Override
  @Transactional
  public Optional<PermissionSnapshot> load(Long userId, String usernameHint) {
    if (userId != null && userId > 0) {
      return userRepository.findByIdOptional(userId).flatMap(this::snapshotFromDbUser);
    }
    if (usernameHint == null || usernameHint.isBlank()) {
      return Optional.empty();
    }
    return load(usernameHint)
        .filter(snapshot -> userId == null || Objects.equals(userId, snapshot.userId()));
  }

  private Optional<PermissionSnapshot> snapshotFromDbUser(SysUser user) {
    if (user == null || user.userStatus != UserStatus.ENABLED) {
      return Optional.empty();
    }
    var roles = new LinkedHashSet<>(userRepository.findRoleCodesByUsername(user.username));
    var permissions = new LinkedHashSet<>(userRepository.findPermissionCodesByUsername(user.username));
    var attributes = new LinkedHashMap<String, Object>();
    attributes.put(PrincipalAttributeKeys.SOURCE, "db");
    attributes.put(PrincipalAttributeKeys.DISPLAY_NAME, user.nickname);
    attributes.put(PrincipalAttributeKeys.ROLES, roles);
    attributes.put(PrincipalAttributeKeys.PERMISSIONS, permissions);
    attributes.put(PrincipalAttributeKeys.USER_ID, user.id);
    attributes.put(
        PrincipalAttributeKeys.AUTHORITY_VERSION, authorityVersionStore.versionFor(user.username));
    return Optional.of(
        new PermissionSnapshot(
            user.username,
            user.nickname == null || user.nickname.isBlank() ? user.username : user.nickname,
            dbUserType,
            roles,
            permissions,
            authorityVersionStore.versionFor(user.username),
            attributes,
            user.id));
  }

  private PermissionSnapshot snapshotFromConfig(
      ConfigUserAccountConfig.ConfigUser entry, String lookupUsername) {
    var roles =
        new LinkedHashSet<>(
            entry.roles().orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
    var permissions =
        new LinkedHashSet<>(
            entry.permissions().orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
    var displayName = entry.displayName().orElse(entry.username());
    var version = authorityVersionStore.versionFor(lookupUsername);
    var principalType = entry.principalUserType().orElse(configUserFallbackType);
    var attributes = new LinkedHashMap<String, Object>();
    attributes.put(PrincipalAttributeKeys.SOURCE, "config");
    attributes.put(PrincipalAttributeKeys.DISPLAY_NAME, displayName);
    attributes.put(PrincipalAttributeKeys.ROLES, roles);
    attributes.put(PrincipalAttributeKeys.PERMISSIONS, permissions);
    attributes.put(PrincipalAttributeKeys.USER_ID, ConfigUserAuthorityId.forUsername(entry.username()));
    attributes.put(PrincipalAttributeKeys.AUTHORITY_VERSION, version);
    return new PermissionSnapshot(
        entry.username(),
        displayName.isBlank() ? entry.username() : displayName,
        principalType,
        roles,
        permissions,
        version,
        attributes,
        ConfigUserAuthorityId.forUsername(entry.username()));
  }

  // DB permissions/roles are resolved via UserRepository queries to avoid N+1 lazy loads.
}
