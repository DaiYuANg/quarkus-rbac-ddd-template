package com.github.DaiYuANg.application.audit;

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
        var snapshot = auditSnapshotProvider.snapshot();
        var log = new SysLoginLog();
        log.username = username;
        log.success = true;
        log.reason = null;
        log.remoteIp = remoteIp != null ? remoteIp : snapshot.remoteIp();
        log.userAgent = userAgent != null ? userAgent : snapshot.userAgent();
        log.loginAt = Instant.now();
        repository.persist(log);
    }

    @Transactional
    public void recordFailure(String username, String remoteIp, String userAgent, String reason) {
        var snapshot = auditSnapshotProvider.snapshot();
        var log = new SysLoginLog();
        log.username = username;
        log.success = false;
        log.reason = reason;
        log.remoteIp = remoteIp != null ? remoteIp : snapshot.remoteIp();
        log.userAgent = userAgent != null ? userAgent : snapshot.userAgent();
        log.loginAt = Instant.now();
        repository.persist(log);
    }
}
