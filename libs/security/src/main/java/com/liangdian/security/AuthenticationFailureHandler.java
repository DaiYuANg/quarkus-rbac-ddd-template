package com.liangdian.security;

import com.liangdian.common.exception.BizException;

public interface AuthenticationFailureHandler {
    BizException toException(LoginAuthenticationRequest request, AuthenticationProviderResult result);

    BizException unsupported(LoginAuthenticationRequest request);
}
