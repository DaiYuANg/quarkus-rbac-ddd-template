package com.github.DaiYuANg.security.authorization;

public interface AuthorizationAuditor {
    void auditDenied(AuthorizationDecision decision);
}
