package com.github.DaiYuANg.application.auth.profile;

import com.github.DaiYuANg.api.dto.response.UserDetailVo;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.security.CurrentAuthenticatedUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class UserProfileResolutionService {

  private final Instance<UserProfileProvider> providers;

  @Inject
  public UserProfileResolutionService(Instance<UserProfileProvider> providers) {
    this.providers = providers;
  }

  public UserDetailVo resolve(CurrentAuthenticatedUser user) {
    return orderedProviders()
        .filter(p -> p.supports(user))
        .findFirst()
        .map(p -> p.buildProfile(user))
        .orElseThrow(
            () ->
                new BizException(
                    ResultCode.DATA_NOT_FOUND,
                    "no profile provider for user: " + user.username()));
  }

  private java.util.stream.Stream<UserProfileProvider> orderedProviders() {
    return StreamSupport.stream(providers.spliterator(), false)
        .sorted(Comparator.comparingInt(UserProfileProvider::order));
  }
}
