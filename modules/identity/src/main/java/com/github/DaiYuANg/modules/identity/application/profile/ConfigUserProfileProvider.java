package com.github.DaiYuANg.modules.identity.application.profile;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Profile for {@code app.security.config-users}: no DB row. Permissions = full permission catalog
 * (runtime list in Redis), not the subset stored on the config entry.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ConfigUserProfileProvider implements UserProfileProvider {

  private static final String CONFIG_USER_TYPE = "CONFIG";

  private final PermissionCatalogStore permissionCatalogStore;
  private final AuthorityVersionStore authorityVersionStore;

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean supports(CurrentAuthenticatedUser user) {
    return CONFIG_USER_TYPE.equalsIgnoreCase(safeUserType(user));
  }

  @Override
  public UserDetailVo buildProfile(CurrentAuthenticatedUser user) {
    var permissions =
        permissionCatalogStore.getAll().stream()
            .map(e -> e.code())
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    var roleCodes =
        user.roles() == null
            ? new LinkedHashSet<String>()
            : user.roles().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    var nickname =
        user.displayName() == null || user.displayName().isBlank()
            ? user.username()
            : user.displayName();
    var authorityKey =
        authorityVersionStore.currentVersion()
            + ":"
            + UserDetailVo.encodeAuthorityKey(permissions, roleCodes);
    return new UserDetailVo(null, user.username(), nickname, permissions, roleCodes, authorityKey);
  }

  private static String safeUserType(CurrentAuthenticatedUser user) {
    return user.userType() == null ? "" : user.userType().trim();
  }
}
