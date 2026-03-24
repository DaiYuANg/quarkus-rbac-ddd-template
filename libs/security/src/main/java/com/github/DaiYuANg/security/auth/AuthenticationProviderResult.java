package com.github.DaiYuANg.security.auth;

import com.github.DaiYuANg.common.constant.ResultCode;

public record AuthenticationProviderResult(
    AuthenticationDecision decision,
    AuthenticationResult authentication,
    ResultCode failureCode,
    String failureMessage
) {
    public static AuthenticationProviderResult success(AuthenticationResult result) {
        return new AuthenticationProviderResult(AuthenticationDecision.SUCCESS, result, null, null);
    }

    public static AuthenticationProviderResult abstain() {
        return new AuthenticationProviderResult(AuthenticationDecision.ABSTAIN, null, null, null);
    }

    public static AuthenticationProviderResult failure(ResultCode code) {
        return new AuthenticationProviderResult(AuthenticationDecision.FAILURE, null, code, code.message());
    }

    public static AuthenticationProviderResult failure(ResultCode code, String message) {
        return new AuthenticationProviderResult(AuthenticationDecision.FAILURE, null, code, message);
    }
}
