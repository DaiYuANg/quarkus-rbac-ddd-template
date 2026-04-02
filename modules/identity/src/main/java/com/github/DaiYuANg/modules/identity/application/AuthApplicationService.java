package com.github.DaiYuANg.modules.identity.application;

import com.github.DaiYuANg.audit.support.AuditSnapshot;
import com.github.DaiYuANg.audit.support.AuditSnapshotProvider;
import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.cache.LoginAttemptStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.identity.application.mapper.MeResponseMapper;
import com.github.DaiYuANg.modules.identity.application.dto.request.LoginRequest;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponse;
import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.modules.identity.application.port.AdminTokenIssuerPort;
import com.github.DaiYuANg.modules.identity.application.port.AuthenticationLifecyclePort;
import com.github.DaiYuANg.modules.identity.application.profile.UserProfileResolutionService;
import com.github.DaiYuANg.security.access.CurrentUserAccess;
import com.github.DaiYuANg.security.auth.RefreshTokenAuthenticationRequest;
import com.github.DaiYuANg.security.auth.UsernamePasswordAuthenticationRequest;
import com.github.DaiYuANg.security.config.AuthSecurityConfig;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.token.PrincipalAttributesSerializer;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.IdentityProviderManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.LinkedHashSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Authentication application service.
 *
 * <p>Coordinates:
 *
 * <ul>
 *   <li>Quarkus {@code IdentityProviderManager} authentication (super-admin + DB user + refresh
 *       token)
 *   <li>refresh token rotation and revocation
 *   <li>publishing Valkey permission snapshots on login/refresh
 * </ul>
 *
 * <p>Security note: permission truth is resolved through snapshot augmentation; JWT claims are used
 * for identity transport and compatibility with endpoint-level checks.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class AuthApplicationService {
  private final UserRepository userRepository;
  private final IdentityProviderManager identityProviderManager;
  private final AdminTokenIssuerPort tokenIssuer;
  private final LoginAttemptStore loginAttemptStore;
  private final AuthorityVersionStore authorityVersionStore;
  private final Event<LoginAuditEvent> loginAuditEvent;
  private final AuditSnapshotProvider auditSnapshotProvider;
  private final AuthSecurityConfig authSecurityConfig;
  private final AuthenticationLifecyclePort authenticationLifecycle;
  private final CurrentUserAccess currentUserAccess;
  private final UserProfileResolutionService userProfileResolutionService;
  private final MeResponseMapper meResponseMapper;
  private final PrincipalAttributesSerializer principalAttributesSerializer;

  @Transactional
  public SystemAuthenticationToken login(@NonNull LoginRequest req) {
    val username = Strings.nullToEmpty(req.username()).trim();
    log.atDebug().addKeyValue("username", username).log("login attempt");
    val snapshot = auditSnapshotProvider.snapshot();
    if (loginAttemptStore.isLocked(username)) {
      log.atDebug()
          .addKeyValue("username", username)
          .addKeyValue("reason", "account_locked")
          .log("login rejected");
      loginAuditEvent.fireAsync(
          LoginAuditEvent.failure(
              username,
              ResultCode.ACCOUNT_TEMPORARILY_LOCKED.message(),
              MoreObjects.firstNonNull(snapshot.remoteIp(), ""),
              MoreObjects.firstNonNull(snapshot.userAgent(), "")));
      throw new BizException(ResultCode.ACCOUNT_TEMPORARILY_LOCKED);
    }
    try {
      val result =
          authenticate(
              new UsernamePasswordAuthenticationRequest(username, req.password()),
              ResultCode.USERNAME_OR_PASSWORD_INVALID);
      log.atDebug()
          .addKeyValue("username", username)
          .log("login success");
      authenticationLifecycle.publishSnapshot(result);
      loginAttemptStore.clear(username);
      loginAuditEvent.fireAsync(
          LoginAuditEvent.success(
              username,
              MoreObjects.firstNonNull(snapshot.remoteIp(), ""),
              MoreObjects.firstNonNull(snapshot.userAgent(), "")));
      return tokenIssuer.issue(result);
    } catch (BizException ex) {
      log.atDebug()
          .addKeyValue("username", username)
          .addKeyValue("reason", ex.getMessage())
          .log("login failure");
      onLoginFailure(username, ex.getMessage(), snapshot);
      throw ex;
    }
  }

  public UserDetailVo profile(@NonNull String username) {
    val current =
        currentUserAccess
            .currentUser()
            .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
    if (!Objects.equal(username, current.username())) {
      throw new BizException(ResultCode.FORBIDDEN);
    }
    return userProfileResolutionService.resolve(current);
  }

  @Transactional
  public MeResponse me(@NonNull String username) {
    val current =
        currentUserAccess
            .currentUser()
            .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
    if (!Objects.equal(username, current.username())) {
      throw new BizException(ResultCode.FORBIDDEN);
    }

    val user = userRepository.findByUsernameWithRbacGraph(username).orElse(null);
    if (user != null) {
      return meResponseMapper.fromDbUser(user, current.displayName(), current.permissions());
    }

    return meResponseMapper.fromCurrentUser(current, current.displayName(), current.permissions());
  }

  public long refreshTokenTtlSeconds() {
    return authSecurityConfig.refreshTokenTtlSeconds();
  }

  public void logout(@NonNull String username, @NonNull String refreshToken) {
    log.atDebug().log("logout (revoke refresh token)");
    val owner =
        authenticationLifecycle
            .findRefreshTokenOwner(refreshToken)
            .orElseThrow(() -> new BizException(ResultCode.REFRESH_TOKEN_INVALID));
    if (!Objects.equal(owner, username)) {
      throw new BizException(ResultCode.FORBIDDEN);
    }
    authenticationLifecycle.revokeRefreshToken(refreshToken);
  }

  public SystemAuthenticationToken refreshToken(@NonNull String refreshToken) {
    log.atDebug().log("refresh token attempt");
    val result =
        authenticate(
            new RefreshTokenAuthenticationRequest(refreshToken), ResultCode.REFRESH_TOKEN_INVALID);
    log.atDebug().addKeyValue("username", result.username()).log("refresh token success");
    authenticationLifecycle.revokeRefreshToken(refreshToken);
    authenticationLifecycle.publishSnapshot(result);
    return tokenIssuer.issue(result);
  }

  public String checkAuthorityVersion(@NonNull String username) {
    val current =
        currentUserAccess
            .currentUser()
            .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
    if (!Objects.equal(username, current.username())) {
      throw new BizException(ResultCode.FORBIDDEN);
    }
    val user = userRepository.findByUsername(username).orElse(null);
    if (user == null) {
      return composeAuthorityVersion(
          new LinkedHashSet<>(current.permissions()), new LinkedHashSet<>(current.roles()));
    }
    val permissions = new LinkedHashSet<>(userRepository.findPermissionCodesByUsername(username));
    val roles = new LinkedHashSet<>(userRepository.findRoleCodesByUsername(username));
    return composeAuthorityVersion(permissions, roles);
  }

  private void onLoginFailure(
      @NonNull String username, @NonNull String reason, @NonNull AuditSnapshot snapshot) {
    val attempts =
        loginAttemptStore.incrementFailure(
            username, Duration.ofSeconds(authSecurityConfig.loginFailureLockSeconds()));
    if (attempts >= authSecurityConfig.loginFailureMaxAttempts()) {
      log.atDebug()
          .addKeyValue("username", username)
          .addKeyValue("attempts", attempts)
          .log("login locked");
      loginAttemptStore.lock(
          username, Duration.ofSeconds(authSecurityConfig.loginFailureLockSeconds()));
    }
    loginAuditEvent.fireAsync(
        LoginAuditEvent.failure(
            username,
            reason,
            MoreObjects.firstNonNull(snapshot.remoteIp(), ""),
            MoreObjects.firstNonNull(snapshot.userAgent(), "")));
  }

  private @NonNull String composeAuthorityVersion(
      @NonNull java.util.Set<String> permissions, @NonNull java.util.Set<String> roleCodes) {
    return authorityVersionStore.currentVersion()
        + ":"
        + UserDetailVo.encodeAuthorityKey(permissions, roleCodes);
  }

  private AuthenticatedUser authenticate(
      @NonNull com.github.DaiYuANg.security.auth.LoginAuthenticationRequest request,
      @NonNull ResultCode defaultFailureCode) {
    try {
      return principalAttributesSerializer.toAuthenticatedUser(
          identityProviderManager.authenticateBlocking(request));
    } catch (BizException ex) {
      throw ex;
    } catch (AuthenticationFailedException | IllegalArgumentException ex) {
      throw new BizException(defaultFailureCode);
    }
  }

  // DB permissions/roles are resolved via UserRepository queries to avoid N+1 lazy loads.
}
