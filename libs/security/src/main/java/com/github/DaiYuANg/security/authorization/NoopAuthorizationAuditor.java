package com.github.DaiYuANg.security.authorization;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@DefaultBean
public class NoopAuthorizationAuditor implements AuthorizationAuditor {
  @Override
  public void auditDenied(AuthorizationDecision decision) {
    // no-op
  }
}
