package com.github.DaiYuANg.security.auth;

import com.github.DaiYuANg.common.exception.BizException;

public interface AuthenticationFailureHandler {
  BizException toException(LoginAuthenticationRequest request, AuthenticationProviderResult result);

  BizException unsupported(LoginAuthenticationRequest request);
}
