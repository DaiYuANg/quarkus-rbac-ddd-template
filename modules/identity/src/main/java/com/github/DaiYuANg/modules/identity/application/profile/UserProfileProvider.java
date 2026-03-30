package com.github.DaiYuANg.modules.identity.application.profile;

import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;

/**
 * Resolves {@link UserDetailVo} for the current principal. One implementation per login identity
 * source (super admin, DB user, future providers).
 */
public interface UserProfileProvider {

  int order();

  boolean supports(CurrentAuthenticatedUser user);

  UserDetailVo buildProfile(CurrentAuthenticatedUser user);
}
