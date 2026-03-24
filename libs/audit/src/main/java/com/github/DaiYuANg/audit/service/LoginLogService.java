package com.github.DaiYuANg.audit.service;

import com.github.DaiYuANg.audit.entity.SysLoginLog;
import com.github.DaiYuANg.audit.repository.LoginLogRepository;
import com.github.DaiYuANg.audit.support.AuditSnapshotProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LoginLogService {
  private final LoginLogRepository repository;
  private final AuditSnapshotProvider auditSnapshotProvider;

  @Transactional
  public void recordSuccess(String username) {
    recordSuccess(username, null, null);
  }

  @Transactional
  public void recordFailure(String username, String reason) {
    recordFailure(username, null, null, reason);
  }

  @Transactional
  public void recordSuccess(String username, String remoteIp, String userAgent) {
    var ipAndUa = resolveRemoteIpAndUserAgent(remoteIp, userAgent);
    var log = new SysLoginLog();
    log.username = username;
    log.success = true;
    log.reason = null;
    log.remoteIp = ipAndUa.remoteIp();
    log.userAgent = ipAndUa.userAgent();
    log.loginAt = Instant.now();
    repository.persist(log);
  }

  @Transactional
  public void recordFailure(String username, String remoteIp, String userAgent, String reason) {
    var ipAndUa = resolveRemoteIpAndUserAgent(remoteIp, userAgent);
    var log = new SysLoginLog();
    log.username = username;
    log.success = false;
    log.reason = reason;
    log.remoteIp = ipAndUa.remoteIp();
    log.userAgent = ipAndUa.userAgent();
    log.loginAt = Instant.now();
    repository.persist(log);
  }

  /**
   * Resolve remoteIp/userAgent from params or current request. Only calls AuditSnapshotProvider
   * when needed; avoids it in async context (e.g. @ObservesAsync) where request context is absent.
   */
  private record IpAndUserAgent(String remoteIp, String userAgent) {}

  private IpAndUserAgent resolveRemoteIpAndUserAgent(String remoteIp, String userAgent) {
    if (remoteIp != null && userAgent != null) {
      return new IpAndUserAgent(remoteIp, userAgent);
    }
    var snapshot = auditSnapshotProvider.snapshot();
    return new IpAndUserAgent(
        remoteIp != null ? remoteIp : snapshot.remoteIp(),
        userAgent != null ? userAgent : snapshot.userAgent());
  }
}
