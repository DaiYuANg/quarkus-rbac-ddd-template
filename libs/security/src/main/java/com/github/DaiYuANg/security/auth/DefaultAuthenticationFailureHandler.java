package com.github.DaiYuANg.security.auth;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultAuthenticationFailureHandler implements AuthenticationFailureHandler {
  @Override
  public BizException toException(
      LoginAuthenticationRequest request, AuthenticationProviderResult result) {
    if (result == null || result.failureCode() == null) {
      return new BizException(ResultCode.USERNAME_OR_PASSWORD_INVALID);
    }
    return new BizException(result.failureCode(), result.failureMessage());
  }

  @Override
  public BizException unsupported(LoginAuthenticationRequest request) {
    return new BizException(ResultCode.USERNAME_OR_PASSWORD_INVALID);
  }
}
