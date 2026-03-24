package com.github.DaiYuANg.application.auth.profile;

import com.github.DaiYuANg.api.dto.response.UserDetailVo;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;

/**
 * Resolves {@link UserDetailVo} for the current principal. One implementation per login identity
 * source (config user, DB user, future providers).
 */
public interface UserProfileProvider {

  int order();

  boolean supports(CurrentAuthenticatedUser user);

  UserDetailVo buildProfile(CurrentAuthenticatedUser user);
}
