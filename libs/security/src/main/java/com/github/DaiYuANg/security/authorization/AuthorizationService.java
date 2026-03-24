package com.github.DaiYuANg.security.authorization;

import com.github.DaiYuANg.security.authorization.PermissionDescriptor;

public interface AuthorizationService {
    AuthorizationDecision decide(String code);
    AuthorizationDecision decide(String resource, String action);
    AuthorizationDecision decide(PermissionDescriptor descriptor);

    void check(String code);
    void check(String resource, String action);
    void check(PermissionDescriptor descriptor);
    void checkAny(String... codes);
    void checkAll(String... codes);

    boolean isAllowed(String code);
    boolean isAllowed(String resource, String action);
}
