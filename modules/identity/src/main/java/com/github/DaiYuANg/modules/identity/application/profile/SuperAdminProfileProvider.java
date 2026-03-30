package com.github.DaiYuANg.modules.identity.application.profile;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import com.github.DaiYuANg.security.identity.SecurityPrincipalKinds;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SuperAdminProfileProvider implements UserProfileProvider {
  private final AuthorityVersionStore authorityVersionStore;

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean supports(@NonNull CurrentAuthenticatedUser user) {
    return SecurityPrincipalKinds.UserType.SUPER_ADMIN.equalsIgnoreCase(safeUserType(user));
  }

  @Override
  public UserDetailVo buildProfile(@NonNull CurrentAuthenticatedUser user) {
    val permissions =
        normalizeCodes(user.permissions() == null ? java.util.Set.of() : user.permissions());
    val roleCodes = normalizeCodes(user.roles() == null ? java.util.Set.of() : user.roles());
    val nickname =
        user.displayName() == null || user.displayName().isBlank()
            ? user.username()
            : user.displayName();
    val authorityKey =
        authorityVersionStore.currentVersion()
            + ":"
            + UserDetailVo.encodeAuthorityKey(permissions, roleCodes);
    return new UserDetailVo(null, user.username(), nickname, permissions, roleCodes, authorityKey);
  }

  private LinkedHashSet<String> normalizeCodes(Set<String> values) {
    return values.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private static String safeUserType(@NonNull CurrentAuthenticatedUser user) {
    return user.userType() == null ? "" : user.userType().trim();
  }
}
