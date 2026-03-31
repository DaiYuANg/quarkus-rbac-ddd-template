package com.github.DaiYuANg.modules.identity.application.profile;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.modules.identity.application.mapper.UserDetailVoMapper;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import com.github.DaiYuANg.security.identity.SecurityPrincipalKinds;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SuperAdminProfileProvider implements UserProfileProvider {
  private final AuthorityVersionStore authorityVersionStore;
  private final UserDetailVoMapper userDetailVoMapper;

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
    val nickname = MoreObjects.firstNonNull(normalize(user.displayName()), user.username());
    val authorityKey =
        authorityVersionStore.currentVersion()
            + ":"
            + UserDetailVo.encodeAuthorityKey(permissions, roleCodes);
    return userDetailVoMapper.fromCurrentUser(
        user, nickname, permissions, roleCodes, authorityKey, null);
  }

  private LinkedHashSet<String> normalizeCodes(@NonNull Set<String> values) {
    return values.stream()
        .filter(Objects::nonNull)
        .map(StringUtils::trimToNull)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private static String safeUserType(@NonNull CurrentAuthenticatedUser user) {
    return Strings.nullToEmpty(normalize(user.userType()));
  }

  private static String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
