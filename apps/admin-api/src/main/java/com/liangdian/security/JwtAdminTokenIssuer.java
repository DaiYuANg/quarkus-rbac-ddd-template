package com.liangdian.security;

import com.liangdian.api.dto.response.SystemAuthenticationToken;
import com.liangdian.redis.AuthorityVersionStore;
import com.liangdian.redis.RefreshTokenStore;
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
