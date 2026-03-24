package com.github.DaiYuANg.security.authorization;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.security.access.CurrentUserAccess;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultAuthorizationService implements AuthorizationService {
    private final CurrentUserAccess currentUserAccess;
    private final AuthorizationAuditor authorizationAuditor;

    @Override
    public AuthorizationDecision decide(String code) {
        return decide(PermissionDescriptor.ofCode(code));
    }

    @Override
    public AuthorizationDecision decide(String resource, String action) {
        return decide(PermissionDescriptor.of(resource, action));
    }

    @Override
    public AuthorizationDecision decide(PermissionDescriptor descriptor) {
        var current = currentUserAccess.currentUser().orElse(null);
        if (current == null) {
            return AuthorizationDecision.deny(descriptor, null, "anonymous");
        }
        return current.permissions().contains(descriptor.code())
            ? AuthorizationDecision.allow(descriptor, current, "snapshot")
            : AuthorizationDecision.deny(descriptor, current, "permission-miss");
    }

    @Override
    public void check(String code) {
        check(PermissionDescriptor.ofCode(code));
    }

    @Override
    public void check(String resource, String action) {
        check(PermissionDescriptor.of(resource, action));
    }

    @Override
    public void check(PermissionDescriptor descriptor) {
        var decision = decide(descriptor);
        if (!decision.allowed()) {
            authorizationAuditor.auditDenied(decision);
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    @Override
    public void checkAny(String... codes) {
        AuthorizationDecision lastDenied = null;
        if (codes != null) {
            for (String code : codes) {
                var decision = decide(code);
                if (decision.allowed()) {
                    return;
                }
                lastDenied = decision;
            }
        }
        if (lastDenied == null) {
            lastDenied = AuthorizationDecision.deny(PermissionDescriptor.ofCode(""), currentUserAccess.currentUser().orElse(null), "no-permission-candidates");
        }
        authorizationAuditor.auditDenied(lastDenied);
        throw new BizException(ResultCode.FORBIDDEN);
    }

    @Override
    public void checkAll(String... codes) {
        if (codes == null) {
            return;
        }
        for (String code : codes) {
            check(code);
        }
    }

    @Override
    public boolean isAllowed(String code) {
        return decide(code).allowed();
    }

    @Override
    public boolean isAllowed(String resource, String action) {
        return decide(resource, action).allowed();
    }
}
