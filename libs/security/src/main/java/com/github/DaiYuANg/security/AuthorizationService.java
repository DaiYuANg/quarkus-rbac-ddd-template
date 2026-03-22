package com.github.DaiYuANg.security;

public interface AuthorizationService {
    AuthorizationDecision decide(String code);
    AuthorizationDecision decide(String domain, String resource, String action);
    AuthorizationDecision decide(PermissionDescriptor descriptor);

    void check(String code);
    void check(String domain, String resource, String action);
    void check(PermissionDescriptor descriptor);
    void checkAny(String... codes);
    void checkAll(String... codes);

    boolean isAllowed(String code);
    boolean isAllowed(String domain, String resource, String action);
}
