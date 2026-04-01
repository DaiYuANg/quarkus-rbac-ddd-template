package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.security.runtime.identity.AdminDbUserAuthoritySupport;
import com.github.DaiYuANg.security.auth.RefreshTokenAuthenticationRequest;
import com.github.DaiYuANg.security.identity.QuarkusSecurityIdentityFactory;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotLoader;
import com.google.common.base.Strings;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminRefreshTokenAuthenticationProvider
    implements IdentityProvider<RefreshTokenAuthenticationRequest> {
  private final RefreshTokenStore refreshTokenStore;
  private final UserRepository userRepository;
  private final AdminDbUserAuthoritySupport userAuthoritySupport;
  private final PermissionSnapshotLoader permissionSnapshotLoader;
  private final QuarkusSecurityIdentityFactory securityIdentityFactory;

  @Override
  public int priority() {
    return 100;
  }

  @Override
  public Class<RefreshTokenAuthenticationRequest> getRequestType() {
    return RefreshTokenAuthenticationRequest.class;
  }

  @Override
  public Uni<SecurityIdentity> authenticate(
      @NonNull RefreshTokenAuthenticationRequest request, AuthenticationRequestContext context) {
    return context.runBlocking(() -> authenticateBlocking(request));
  }

  private SecurityIdentity authenticateBlocking(@NonNull RefreshTokenAuthenticationRequest request) {
    val owner = refreshTokenStore.getOwner(request.refreshToken()).orElse(null);
    if (owner == null
        || (owner.userId() == null && Strings.isNullOrEmpty(normalize(owner.username())))) {
      throw new BizException(ResultCode.REFRESH_TOKEN_INVALID);
    }

    if (owner.userId() != null && owner.userId() > 0) {
      return userRepository
          .findByIdOptional(owner.userId())
          .map(this::authenticateDbUser)
          .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    }

    val username = normalize(owner.username());
    if (username == null) {
      throw new BizException(ResultCode.DATA_NOT_FOUND);
    }

    return userRepository
        .findByUsername(username)
        .map(this::authenticateDbUser)
        .orElseGet(
            () ->
                permissionSnapshotLoader
                    .load(owner.userId(), username)
                    .map(snapshot -> securityIdentityFactory.create(snapshot.toAuthenticatedUser()))
                    .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND)));
  }

  private SecurityIdentity authenticateDbUser(@NonNull SysUser user) {
    if (user.userStatus != UserStatus.ENABLED) {
      throw new BizException(ResultCode.USER_ACCESS_BLOCKED);
    }
    return securityIdentityFactory.create(userAuthoritySupport.authenticatedUser(user));
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}


