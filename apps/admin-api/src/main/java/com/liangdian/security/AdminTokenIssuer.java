package com.liangdian.security;

import com.liangdian.api.dto.response.SystemAuthenticationToken;

public interface AdminTokenIssuer extends TokenIssuer<SystemAuthenticationToken>, RefreshTokenAuthenticator<SystemAuthenticationToken> {
    @Override
    SystemAuthenticationToken issue(AuthenticatedUser user);

    @Override
    SystemAuthenticationToken authenticate(String refreshToken);
}
