package com.github.DaiYuANg.security;

import com.github.DaiYuANg.api.dto.response.SystemAuthenticationToken;

public interface AdminTokenIssuer extends TokenIssuer<SystemAuthenticationToken>, RefreshTokenAuthenticator<SystemAuthenticationToken> {
    @Override
    SystemAuthenticationToken issue(AuthenticatedUser user);

    @Override
    SystemAuthenticationToken authenticate(String refreshToken);
}
