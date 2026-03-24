package com.github.DaiYuANg.modules.identity.application.profile;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

/**
 * Profile backed by {@code sys_user} and RBAC graph (roles, permission groups).
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DbUserProfileProvider implements UserProfileProvider {

  private static final String CONFIG_USER_TYPE = "CONFIG";

  private final UserRepository userRepository;
  private final AuthorityVersionStore authorityVersionStore;

  @Override
  public int order() {
    return 200;
  }

  @Override
  public boolean supports(CurrentAuthenticatedUser user) {
    return !CONFIG_USER_TYPE.equalsIgnoreCase(
        user.userType() == null ? "" : user.userType().trim());
  }

  @Override
  public UserDetailVo buildProfile(CurrentAuthenticatedUser user) {
    var dbUser =
        userRepository
            .findByUsername(user.username())
            .orElseThrow(
                () ->
                    new BizException(
                        ResultCode.DATA_NOT_FOUND, "user not found: " + user.username()));
    var detail = toUserDetail(dbUser);
    var authorityKey =
        authorityVersionStore.currentVersion()
            + ":"
            + UserDetailVo.encodeAuthorityKey(detail.permissions(), detail.roleCodes());
    return new UserDetailVo(
        detail.userid(),
        detail.username(),
        detail.nickname(),
        detail.permissions(),
        detail.roleCodes(),
        authorityKey);
  }

  private UserDetailVo toUserDetail(com.github.DaiYuANg.identity.entity.SysUser user) {
    var permissions = user.roles.stream()
        .flatMap(r -> r.permissionGroups.stream())
        .flatMap(g -> g.permissions.stream())
        .map(p -> p.code)
        .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    var roleCodes = user.roles.stream()
        .map(r -> r.code)
        .filter(java.util.Objects::nonNull)
        .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    return new UserDetailVo(
        user.id,
        user.username,
        user.nickname,
        permissions,
        roleCodes,
        UserDetailVo.encodeAuthorityKey(permissions, roleCodes));
  }
}
