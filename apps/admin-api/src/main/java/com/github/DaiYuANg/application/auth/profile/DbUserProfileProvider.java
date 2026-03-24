package com.github.DaiYuANg.application.auth.profile;

import com.github.DaiYuANg.api.dto.response.UserDetailVo;
import com.github.DaiYuANg.application.converter.ViewMapper;
import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.security.CurrentAuthenticatedUser;
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
  private final ViewMapper viewMapper;
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
    var detail = viewMapper.toUserDetail(dbUser);
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
}
