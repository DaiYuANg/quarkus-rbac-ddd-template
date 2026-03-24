package com.github.DaiYuANg.security.authorization;

import com.github.DaiYuANg.application.audit.OperationLogService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminAuthorizationAuditor implements AuthorizationAuditor {
    private final OperationLogService operationLogService;

    @Override
    public void auditDenied(AuthorizationDecision decision) {
        var permission = decision.permission();
        var target = permission == null ? "unknown" : permission.code();
        var detail = "authorization denied"
            + ", reason=" + decision.reason()
            + ", resource=" + (permission == null ? "" : permission.resource())
            + ", action=" + (permission == null ? "" : permission.action())
            + ", actor=" + (decision.actorKey() == null ? "anonymous" : decision.actorKey())
            + ", authorityVersion=" + (decision.authorityVersion() == null ? "" : decision.authorityVersion())
            + ", source=" + (decision.source() == null ? "" : decision.source());
        operationLogService.record(
            "authorization",
            "deny",
            target,
            false,
            detail
        );
    }
}
