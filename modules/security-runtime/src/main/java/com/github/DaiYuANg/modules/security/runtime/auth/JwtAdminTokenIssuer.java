package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.token.JwtTokenService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class JwtAdminTokenIssuer implements AdminTokenIssuer {
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenStore refreshTokenStore;
    private final AuthorityVersionStore authorityVersionStore;

    @Override
    public SystemAuthenticationToken issue(AuthenticatedUser user) {
        var authorityVersion = authorityVersionStore.versionFor(user.username());
        var accessToken = jwtTokenService.generate(user, authorityVersion);
        var refreshToken = jwtTokenService.generateRefreshToken();
        refreshTokenStore.save(refreshToken, user.username(), jwtTokenService.refreshTokenTtl());
        return new SystemAuthenticationToken(
            accessToken,
            refreshToken,
            "Bearer",
            jwtTokenService.accessTokenExpiresIn(),
            authorityVersion
        );
    }

    @Override
    public SystemAuthenticationToken authenticate(String refreshToken) {
        throw new UnsupportedOperationException("Refresh token authentication is delegated to provider chain");
    }
}
