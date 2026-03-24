package com.github.DaiYuANg.modules.example.infrastructure.security;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleBuyerContext;
import com.github.DaiYuANg.security.access.CurrentUserAccess;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class QuarkusExampleBuyerContext implements ExampleBuyerContext {

  private final CurrentUserAccess currentUserAccess;

  @Override
  public String requireBuyerUsername() {
    return currentUserAccess
        .currentUser()
        .map(u -> u.username())
        .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
  }
}
