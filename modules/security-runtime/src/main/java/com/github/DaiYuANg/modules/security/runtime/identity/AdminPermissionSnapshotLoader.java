package com.github.DaiYuANg.modules.security.runtime.identity;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
      var roles = roleCodes(user);
      var permissions = permissionIdentifiers(user);
      var attributes = new LinkedHashMap<String, Object>();
      attributes.put(PrincipalAttributeKeys.SOURCE, "db");
      attributes.put(PrincipalAttributeKeys.DISPLAY_NAME, user.nickname);
      attributes.put(PrincipalAttributeKeys.ROLES, roles);
      attributes.put(PrincipalAttributeKeys.PERMISSIONS, permissions);
      attributes.put(
          PrincipalAttributeKeys.AUTHORITY_VERSION, authorityVersionStore.versionFor(username));
      return Optional.of(
          new PermissionSnapshot(
              user.username,
              user.nickname == null || user.nickname.isBlank() ? user.username : user.nickname,
              dbUserType,
              roles,
              permissions,
              authorityVersionStore.versionFor(username),
              attributes,
              user.id));
    }
    return ConfigUserAccounts.find(configUserAccountConfig, username)
        .map(entry -> snapshotFromConfig(entry, username));
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

  private LinkedHashSet<String> permissionIdentifiers(
      com.github.DaiYuANg.identity.entity.SysUser user) {
    return user.roles.stream()
        .flatMap(r -> r.permissionGroups.stream())
        .flatMap(g -> g.permissions.stream())
        .map(p -> p.code)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private LinkedHashSet<String> roleCodes(com.github.DaiYuANg.identity.entity.SysUser user) {
    return user.roles.stream()
        .map(r -> r.code)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
