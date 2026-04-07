package com.github.DaiYuANg.modules.identity.application.profile;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Comparator.comparingInt;

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
                    ResultCode.DATA_NOT_FOUND, "no profile provider for user: " + user.username()));
  }

  private Stream<UserProfileProvider> orderedProviders() {
    return StreamSupport.stream(providers.spliterator(), false)
        .sorted(comparingInt(UserProfileProvider::order));
  }
}
