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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
  public Optional<PermissionSnapshot> load(@NonNull String username) {
    val user = userRepository.findByUsername(username).orElse(null);
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

  private Optional<PermissionSnapshot> snapshotFromDbUser(@NonNull SysUser user) {
    if (user.userStatus != UserStatus.ENABLED) {
      return Optional.empty();
    }
    val roles = new LinkedHashSet<>(userRepository.findRoleCodesByUsername(user.username));
    val permissions =
        new LinkedHashSet<>(userRepository.findPermissionCodesByUsername(user.username));
    val attributes = new LinkedHashMap<String, Object>();
    val version = authorityVersionStore.versionFor(user.username);
    attributes.put(PrincipalAttributeKeys.SOURCE, "db");
    attributes.put(PrincipalAttributeKeys.DISPLAY_NAME, user.nickname);
    attributes.put(PrincipalAttributeKeys.ROLES, roles);
    attributes.put(PrincipalAttributeKeys.PERMISSIONS, permissions);
    attributes.put(PrincipalAttributeKeys.USER_ID, user.id);
    attributes.put(PrincipalAttributeKeys.AUTHORITY_VERSION, version);
    return Optional.of(
        new PermissionSnapshot(
            user.username,
            user.nickname == null || user.nickname.isBlank() ? user.username : user.nickname,
            dbUserType,
            roles,
            permissions,
            version,
            attributes,
            user.id));
  }

  private PermissionSnapshot snapshotFromConfig(
      @NonNull ConfigUserAccountConfig.ConfigUser entry, @NonNull String lookupUsername) {
    val roles =
        new LinkedHashSet<>(
            entry.roles().orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
    val permissions =
        new LinkedHashSet<>(
            entry.permissions().orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
    val displayName = entry.displayName().orElse(entry.username());
    val version = authorityVersionStore.versionFor(lookupUsername);
    val principalType = entry.principalUserType().orElse(configUserFallbackType);
    val attributes = new LinkedHashMap<String, Object>();
    val syntheticUserId = ConfigUserAuthorityId.forUsername(entry.username());
    attributes.put(PrincipalAttributeKeys.SOURCE, "config");
    attributes.put(PrincipalAttributeKeys.DISPLAY_NAME, displayName);
    attributes.put(PrincipalAttributeKeys.ROLES, roles);
    attributes.put(PrincipalAttributeKeys.PERMISSIONS, permissions);
    attributes.put(PrincipalAttributeKeys.USER_ID, syntheticUserId);
    attributes.put(PrincipalAttributeKeys.AUTHORITY_VERSION, version);
    return new PermissionSnapshot(
        entry.username(),
        displayName.isBlank() ? entry.username() : displayName,
        principalType,
        roles,
        permissions,
        version,
        attributes,
        syntheticUserId);
  }

  // DB permissions/roles are resolved via UserRepository queries to avoid N+1 lazy loads.
}
