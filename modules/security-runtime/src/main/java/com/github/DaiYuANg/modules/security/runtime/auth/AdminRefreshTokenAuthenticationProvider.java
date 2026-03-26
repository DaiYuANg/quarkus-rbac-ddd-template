package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.security.auth.AuthenticationProviderResult;
import com.github.DaiYuANg.security.auth.AuthenticationResult;
import com.github.DaiYuANg.security.auth.LoginAuthenticationProvider;
import com.github.DaiYuANg.security.auth.LoginAuthenticationRequest;
import com.github.DaiYuANg.security.auth.RefreshTokenAuthenticationRequest;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotLoader;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@Priority(300)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminRefreshTokenAuthenticationProvider
    implements LoginAuthenticationProvider<RefreshTokenAuthenticationRequest> {
  private final RefreshTokenStore refreshTokenStore;
  private final UserRepository userRepository;
  private final AdminSecurityPrincipalAssembler principalAssembler;
  private final PermissionSnapshotLoader permissionSnapshotLoader;

  @Override
  public String providerId() {
    return "refresh-token";
  }

  @Override
  public boolean supports(LoginAuthenticationRequest request) {
    return request instanceof RefreshTokenAuthenticationRequest;
  }

  @Override
  public AuthenticationProviderResult authenticate(RefreshTokenAuthenticationRequest request) {
    var owner = refreshTokenStore.getOwner(request.refreshToken()).orElse(null);
    if (owner == null
        || (owner.userId() == null && (owner.username() == null || owner.username().isBlank()))) {
      return AuthenticationProviderResult.failure(ResultCode.REFRESH_TOKEN_INVALID);
    }

    if (owner.userId() != null && owner.userId() > 0) {
      return userRepository
          .findByIdOptional(owner.userId())
          .map(this::authenticateDbUser)
          .orElse(AuthenticationProviderResult.failure(ResultCode.DATA_NOT_FOUND));
    }

    var username = owner.username();
    if (username == null || username.isBlank()) {
      return AuthenticationProviderResult.failure(ResultCode.DATA_NOT_FOUND);
    }

    return userRepository
        .findByUsername(username)
        .map(this::authenticateDbUser)
        .orElseGet(
            () ->
                permissionSnapshotLoader
                    .load(owner.userId(), username)
                    .map(
                        snapshot ->
                            AuthenticationProviderResult.success(
                                new AuthenticationResult(
                                    snapshot.toAuthenticatedUser(), providerId())))
                    .orElse(AuthenticationProviderResult.failure(ResultCode.DATA_NOT_FOUND)));
  }

  private AuthenticationProviderResult authenticateDbUser(SysUser user) {
    if (user.userStatus != UserStatus.ENABLED) {
      return AuthenticationProviderResult.failure(ResultCode.USER_ACCESS_BLOCKED);
    }
    return AuthenticationProviderResult.success(
        new AuthenticationResult(principalAssembler.fromDbUser(user), providerId()));
  }
}
