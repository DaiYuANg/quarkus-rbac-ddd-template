package com.github.DaiYuANg.modules.identity.application;

import com.github.DaiYuANg.audit.support.AuditSnapshot;
import com.github.DaiYuANg.audit.support.AuditSnapshotProvider;
import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.cache.LoginAttemptStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.identity.application.dto.request.LoginRequest;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponse;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponseBuilder;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeRoleItem;
import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.modules.identity.application.port.AdminTokenIssuerPort;
import com.github.DaiYuANg.modules.identity.application.port.AuthenticationLifecyclePort;
import com.github.DaiYuANg.modules.identity.application.profile.UserProfileResolutionService;
import com.github.DaiYuANg.security.access.CurrentUserAccess;
import com.github.DaiYuANg.security.auth.LoginAuthenticationManager;
import com.github.DaiYuANg.security.auth.RefreshTokenAuthenticationRequest;
import com.github.DaiYuANg.security.auth.UsernamePasswordAuthenticationRequest;
import com.github.DaiYuANg.security.config.AuthSecurityConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Objects;
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
 *   <li>authentication provider chain (DB users + super admin)
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
  private final LoginAuthenticationManager authenticationManager;
  private final AdminTokenIssuerPort tokenIssuer;
  private final LoginAttemptStore loginAttemptStore;
  private final AuthorityVersionStore authorityVersionStore;
  private final Event<LoginAuditEvent> loginAuditEvent;
  private final AuditSnapshotProvider auditSnapshotProvider;
  private final AuthSecurityConfig authSecurityConfig;
  private final AuthenticationLifecyclePort authenticationLifecycle;
  private final CurrentUserAccess currentUserAccess;
  private final UserProfileResolutionService userProfileResolutionService;

  @Transactional
  public SystemAuthenticationToken login(@NonNull LoginRequest req) {
    val username = req.username() == null ? "" : req.username().trim();
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
              Objects.requireNonNullElse(snapshot.remoteIp(), ""),
              Objects.requireNonNullElse(snapshot.userAgent(), "")));
      throw new BizException(ResultCode.ACCOUNT_TEMPORARILY_LOCKED);
    }
    try {
      val result =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationRequest(username, req.password()));
      log.atDebug()
          .addKeyValue("username", username)
          .addKeyValue("provider", result.providerId())
          .log("login success");
      authenticationLifecycle.publishSnapshot(result.user());
      loginAttemptStore.clear(username);
      loginAuditEvent.fireAsync(
          LoginAuditEvent.success(
              username,
              Objects.requireNonNullElse(snapshot.remoteIp(), ""),
              Objects.requireNonNullElse(snapshot.userAgent(), "")));
      return tokenIssuer.issue(result.user());
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
    if (!username.equals(current.username())) {
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
    if (!username.equals(current.username())) {
      throw new BizException(ResultCode.FORBIDDEN);
    }

    val user = userRepository.findByUsernameWithRbacGraph(username).orElse(null);
    if (user != null) {
      val roles =
          user.roles.stream()
              .map(role -> new MeRoleItem(String.valueOf(role.id), role.name))
              .toList();
      return MeResponseBuilder.builder()
          .id(String.valueOf(user.id))
          .name(current.displayName())
          .email(user.email)
          .roles(roles)
          .permissions(current.permissions())
          .build();
    }

    val configRoles = current.roles().stream().map(role -> new MeRoleItem(role, role)).toList();
    return MeResponseBuilder.builder()
        .id(current.username())
        .name(current.displayName())
        .email(null)
        .roles(configRoles)
        .permissions(current.permissions())
        .build();
  }

  public void logout(@NonNull String refreshToken) {
    log.atDebug().log("logout (revoke refresh token)");
    authenticationLifecycle.revokeRefreshToken(refreshToken);
  }

  public SystemAuthenticationToken refreshToken(@NonNull String refreshToken) {
    log.atDebug().log("refresh token attempt");
    val result =
        authenticationManager.authenticate(new RefreshTokenAuthenticationRequest(refreshToken));
    log.atDebug().addKeyValue("username", result.user().username()).log("refresh token success");
    authenticationLifecycle.revokeRefreshToken(refreshToken);
    authenticationLifecycle.publishSnapshot(result.user());
    return tokenIssuer.issue(result.user());
  }

  public String checkAuthorityVersion(@NonNull String username) {
    val current =
        currentUserAccess
            .currentUser()
            .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
    if (!username.equals(current.username())) {
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
            Objects.requireNonNullElse(snapshot.remoteIp(), ""),
            Objects.requireNonNullElse(snapshot.userAgent(), "")));
  }

  private @NonNull String composeAuthorityVersion(
      java.util.Set<String> permissions, java.util.Set<String> roleCodes) {
    return authorityVersionStore.currentVersion()
        + ":"
        + UserDetailVo.encodeAuthorityKey(permissions, roleCodes);
  }

  // DB permissions/roles are resolved via UserRepository queries to avoid N+1 lazy loads.
}
