package com.github.DaiYuANg.security;

public interface AuthorizationAuditor {
    void auditDenied(AuthorizationDecision decision);
}
