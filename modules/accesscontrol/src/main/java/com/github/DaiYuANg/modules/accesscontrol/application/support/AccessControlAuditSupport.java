package com.github.DaiYuANg.modules.accesscontrol.application.support;

import com.github.DaiYuANg.audit.entity.SysOperationLog;
import com.github.DaiYuANg.audit.repository.OperationLogRepository;
import com.github.DaiYuANg.audit.support.AuditSnapshotProvider;
import com.github.DaiYuANg.cache.AuthorityVersionStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AccessControlAuditSupport {
  private final AuthorityVersionStore authorityVersionStore;
  private final OperationLogRepository operationLogRepository;
  private final AuditSnapshotProvider auditSnapshotProvider;

  public String bumpGlobalVersion() {
    return authorityVersionStore.bumpGlobalVersion();
  }

  public void record(String module, String action, String target, boolean success, String detail) {
    val snapshot = auditSnapshotProvider.snapshot();
    val log = new SysOperationLog();
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
    operationLogRepository.persist(log);
  }
}
