package com.github.DaiYuANg.modules.accesscontrol.application.support;

import com.github.DaiYuANg.audit.entity.SysOperationLog;
import com.github.DaiYuANg.audit.repository.OperationLogRepository;
import com.github.DaiYuANg.audit.support.AuditSnapshotProvider;
import com.github.DaiYuANg.cache.AuthorityVersionStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import lombok.NonNull;
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

  public void recordSuccess(
      @NonNull String module,
      @NonNull String action,
      @NonNull String target,
      @NonNull String detail) {
    bumpGlobalVersion();
    record(
        AccessControlAuditCommandBuilder.builder()
            .module(module)
            .action(action)
            .target(target)
            .success(true)
            .detail(detail)
            .build());
  }

  public void record(@NonNull AccessControlAuditCommand command) {
    val snapshot = auditSnapshotProvider.snapshot();
    val log = new SysOperationLog();
    log.module = command.module();
    log.action = command.action();
    log.target = command.target();
    log.success = command.success();
    log.detail = command.detail();
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
