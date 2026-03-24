package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.modules.identity.application.port.AdminTokenIssuerPort;
import com.github.DaiYuANg.security.auth.RefreshTokenAuthenticator;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.token.TokenIssuer;

public interface AdminTokenIssuer extends AdminTokenIssuerPort, TokenIssuer<SystemAuthenticationToken>, RefreshTokenAuthenticator<SystemAuthenticationToken> {
    @Override
    SystemAuthenticationToken issue(AuthenticatedUser user);

    @Override
    SystemAuthenticationToken authenticate(String refreshToken);
}
