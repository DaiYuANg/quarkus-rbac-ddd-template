package com.github.DaiYuANg.security.auth;

import com.github.DaiYuANg.api.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.security.auth.RefreshTokenAuthenticator;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.token.TokenIssuer;

public interface AdminTokenIssuer extends TokenIssuer<SystemAuthenticationToken>, RefreshTokenAuthenticator<SystemAuthenticationToken> {
    @Override
    SystemAuthenticationToken issue(AuthenticatedUser user);

    @Override
    SystemAuthenticationToken authenticate(String refreshToken);
}
