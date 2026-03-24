package com.github.DaiYuANg.application.auth;

import com.github.DaiYuANg.api.dto.request.LoginRequest;
import com.github.DaiYuANg.api.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.api.dto.response.UserDetailVo;
import com.github.DaiYuANg.application.auth.profile.UserProfileResolutionService;
import com.github.DaiYuANg.application.converter.ViewMapper;
import com.github.DaiYuANg.audit.support.AuditSnapshot;
import com.github.DaiYuANg.audit.support.AuditSnapshotProvider;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.cache.LoginAttemptStore;
import com.github.DaiYuANg.security.auth.AdminAuthenticationLifecycle;
import com.github.DaiYuANg.security.auth.AdminTokenIssuer;
import com.github.DaiYuANg.security.config.AuthSecurityConfig;
import com.github.DaiYuANg.security.access.CurrentUserAccess;
import com.github.DaiYuANg.security.auth.LoginAuthenticationManager;
import com.github.DaiYuANg.security.auth.RefreshTokenAuthenticationRequest;
import com.github.DaiYuANg.security.auth.UsernamePasswordAuthenticationRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthApplicationService {
  private static final Logger log = LoggerFactory.getLogger(AuthApplicationService.class);

  private final UserRepository userRepository;
  private final LoginAuthenticationManager authenticationManager;
  private final AdminTokenIssuer tokenIssuer;
  private final LoginAttemptStore loginAttemptStore;
  private final AuthorityVersionStore authorityVersionStore;
  private final ViewMapper viewMapper;
  private final Event<LoginAuditEvent> loginAuditEvent;
  private final AuditSnapshotProvider auditSnapshotProvider;
  private final AuthSecurityConfig authSecurityConfig;
  private final AdminAuthenticationLifecycle authenticationLifecycle;
  private final CurrentUserAccess currentUserAccess;
  private final UserProfileResolutionService userProfileResolutionService;

  @Transactional
  public SystemAuthenticationToken login(@NonNull LoginRequest req) {
    var username = req.username() == null ? "" : req.username().trim();
    log.atDebug().addKeyValue("username", username).log("login attempt");
    var snapshot = auditSnapshotProvider.snapshot();
    if (loginAttemptStore.isLocked(username)) {
      log.atDebug().addKeyValue("username", username).addKeyValue("reason", "account_locked").log("login rejected");
      loginAuditEvent.fireAsync(LoginAuditEvent.failure(username, ResultCode.ACCOUNT_TEMPORARILY_LOCKED.message(), Objects.requireNonNullElse(snapshot.remoteIp(), ""), Objects.requireNonNullElse(snapshot.userAgent(), "")));
      throw new BizException(ResultCode.ACCOUNT_TEMPORARILY_LOCKED);
    }
    try {
      var result = authenticationManager.authenticate(new UsernamePasswordAuthenticationRequest(username, req.password()));
      log.atDebug().addKeyValue("username", username).addKeyValue("provider", result.providerId()).log("login success");
      authenticationLifecycle.publishSnapshot(result.user());
      loginAttemptStore.clear(username);
      loginAuditEvent.fireAsync(LoginAuditEvent.success(username, Objects.requireNonNullElse(snapshot.remoteIp(), ""), Objects.requireNonNullElse(snapshot.userAgent(), "")));
      return tokenIssuer.issue(result.user());
    } catch (BizException ex) {
      log.atDebug().addKeyValue("username", username).addKeyValue("reason", ex.getMessage()).log("login failure");
      onLoginFailure(username, ex.getMessage(), snapshot);
      throw ex;
    }
  }

  public UserDetailVo profile(String username) {
    var current =
        currentUserAccess
            .currentUser()
            .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
    if (!username.equals(current.username())) {
      throw new BizException(ResultCode.FORBIDDEN);
    }
    return userProfileResolutionService.resolve(current);
  }

  public void logout(String refreshToken) {
    log.atDebug().log("logout (revoke refresh token)");
    authenticationLifecycle.revokeRefreshToken(refreshToken);
  }

  public SystemAuthenticationToken refreshToken(String refreshToken) {
    log.atDebug().log("refresh token attempt");
    var result = authenticationManager.authenticate(new RefreshTokenAuthenticationRequest(refreshToken));
    log.atDebug().addKeyValue("username", result.user().username()).log("refresh token success");
    authenticationLifecycle.revokeRefreshToken(refreshToken);
    authenticationLifecycle.publishSnapshot(result.user());
    return tokenIssuer.issue(result.user());
  }

  public String checkAuthorityVersion(String username) {
    var user = userRepository.findByUsername(username).orElse(null);
    if (user == null) {
      return authorityVersionStore.currentVersion() + ":config";
    }
    return composeAuthorityVersion(viewMapper.permissionIdentifiers(user), viewMapper.roleCodes(user));
  }

  private void onLoginFailure(String username, String reason, AuditSnapshot snapshot) {
    var attempts = loginAttemptStore.incrementFailure(username, Duration.ofSeconds(authSecurityConfig.loginFailureLockSeconds()));
    if (attempts >= authSecurityConfig.loginFailureMaxAttempts()) {
      log.atDebug().addKeyValue("username", username).addKeyValue("attempts", attempts).log("login locked");
      loginAttemptStore.lock(username, Duration.ofSeconds(authSecurityConfig.loginFailureLockSeconds()));
    }
    loginAuditEvent.fireAsync(LoginAuditEvent.failure(username, reason, Objects.requireNonNullElse(snapshot.remoteIp(), ""), Objects.requireNonNullElse(snapshot.userAgent(), "")));
  }

  private @NonNull String composeAuthorityVersion(java.util.Set<String> permissions, java.util.Set<String> roleCodes) {
    return authorityVersionStore.currentVersion() + ":" + UserDetailVo.encodeAuthorityKey(permissions, roleCodes);
  }
}
