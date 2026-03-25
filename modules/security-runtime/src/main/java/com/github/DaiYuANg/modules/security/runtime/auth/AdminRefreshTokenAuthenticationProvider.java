package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.security.auth.AuthenticationProviderResult;
import com.github.DaiYuANg.security.auth.AuthenticationResult;
import com.github.DaiYuANg.security.auth.LoginAuthenticationProvider;
import com.github.DaiYuANg.security.auth.LoginAuthenticationRequest;
import com.github.DaiYuANg.security.auth.RefreshTokenAuthenticationRequest;
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
    return refreshTokenStore
        .getUsername(request.refreshToken())
        .filter(username -> !username.isBlank())
        .map(
            username ->
                userRepository
                    .findByUsername(username)
                    .map(
                        user -> {
                          if (user.userStatus != UserStatus.ENABLED) {
                            return AuthenticationProviderResult.failure(
                                ResultCode.USER_ACCESS_BLOCKED);
                          }
                          return AuthenticationProviderResult.success(
                              new AuthenticationResult(
                                  principalAssembler.fromDbUser(user), providerId()));
                        })
                    .orElseGet(
                        () -> AuthenticationProviderResult.failure(ResultCode.DATA_NOT_FOUND)))
        .orElseGet(() -> AuthenticationProviderResult.failure(ResultCode.REFRESH_TOKEN_INVALID));
  }
}
