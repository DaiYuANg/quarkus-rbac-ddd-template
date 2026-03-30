package com.github.DaiYuANg.modules.identity.application.profile;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVoBuilder;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import com.github.DaiYuANg.security.identity.SecurityPrincipalKinds;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Profile backed by {@code sys_user} and the RBAC graph.
 *
 * <p>Permissions/roles are resolved via repository-level typed queries to avoid initializing the
 * entity association graph (prevents N+1 lazy loads).
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DbUserProfileProvider implements UserProfileProvider {

  private final UserRepository userRepository;
  private final AuthorityVersionStore authorityVersionStore;

  @Override
  public int order() {
    return 200;
  }

  @Override
  public boolean supports(@NonNull CurrentAuthenticatedUser user) {
    return !SecurityPrincipalKinds.UserType.SUPER_ADMIN.equalsIgnoreCase(
        user.userType() == null ? "" : user.userType().trim());
  }

  @Override
  public UserDetailVo buildProfile(@NonNull CurrentAuthenticatedUser user) {
    val dbUser =
        userRepository
            .findByUsername(user.username())
            .orElseThrow(
                () ->
                    new BizException(
                        ResultCode.DATA_NOT_FOUND, "user not found: " + user.username()));
    val detail = toUserDetail(dbUser);
    val authorityKey =
        authorityVersionStore.currentVersion()
            + ":"
            + UserDetailVo.encodeAuthorityKey(detail.permissions(), detail.roleCodes());
    return UserDetailVoBuilder.builder()
        .userid(detail.userid())
        .username(detail.username())
        .nickname(detail.nickname())
        .permissions(detail.permissions())
        .roleCodes(detail.roleCodes())
        .authorityKey(authorityKey)
        .build();
  }

  private UserDetailVo toUserDetail(@NonNull com.github.DaiYuANg.identity.entity.SysUser user) {
    val permissions =
        new LinkedHashSet<>(userRepository.findPermissionCodesByUsername(user.username));
    val roleCodes = new LinkedHashSet<>(userRepository.findRoleCodesByUsername(user.username));
    return UserDetailVoBuilder.builder()
        .userid(user.id)
        .username(user.username)
        .nickname(user.nickname)
        .permissions(permissions)
        .roleCodes(roleCodes)
        .authorityKey(UserDetailVo.encodeAuthorityKey(permissions, roleCodes))
        .build();
  }
}
