package com.github.DaiYuANg.modules.security.runtime.authorization;

import com.github.DaiYuANg.audit.entity.SysOperationLog;
import com.github.DaiYuANg.audit.repository.OperationLogRepository;
import com.github.DaiYuANg.audit.support.AuditSnapshotProvider;
import com.github.DaiYuANg.security.authorization.AuthorizationAuditor;
import com.github.DaiYuANg.security.authorization.AuthorizationDecision;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminAuthorizationAuditor implements AuthorizationAuditor {
  private final OperationLogRepository operationLogRepository;
  private final AuditSnapshotProvider auditSnapshotProvider;

  @Override
  public void auditDenied(AuthorizationDecision decision) {
    var permission = decision.permission();
    var target = permission == null ? "unknown" : permission.code();
    var detail =
        "authorization denied"
            + ", reason="
            + decision.reason()
            + ", resource="
            + (permission == null ? "" : permission.resource())
            + ", action="
            + (permission == null ? "" : permission.action())
            + ", actor="
            + (decision.actorKey() == null ? "anonymous" : decision.actorKey())
            + ", authorityVersion="
            + (decision.authorityVersion() == null ? "" : decision.authorityVersion())
            + ", source="
            + (decision.source() == null ? "" : decision.source());
    var snapshot = auditSnapshotProvider.snapshot();
    var log = new SysOperationLog();
    log.module = "authorization";
    log.action = "deny";
    log.target = target;
    log.success = false;
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
