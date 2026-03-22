package com.liangdian.application.audit;

import com.liangdian.audit.entity.SysOperationLog;
import com.liangdian.audit.repository.OperationLogRepository;
import com.liangdian.audit.support.AuditSnapshotProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class OperationLogService {
    private final OperationLogRepository repository;
    private final AuditSnapshotProvider auditSnapshotProvider;

    @Transactional
    public void record(String module, String action, String target, boolean success, String detail) {
        var snapshot = auditSnapshotProvider.snapshot();
        var log = new SysOperationLog();
        log.module = module;
        log.action = action;
        log.target = target;
        log.success = success;
        log.detail = detail;
        log.operator = snapshot.actorKey();
        log.operatorDisplayName = snapshot.actorDisplayName();
        log.operatorType = snapshot.actorType();
        log.remoteIp = snapshot.remoteIp();
        log.userAgent = snapshot.userAgent();
        log.requestId = snapshot.requestId();
        log.occurredAt = Instant.now();
        repository.persist(log);
    }
}
