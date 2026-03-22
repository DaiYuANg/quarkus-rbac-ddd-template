package com.liangdian.security;

public interface AuthorizationAuditor {
    void auditDenied(AuthorizationDecision decision);
}
