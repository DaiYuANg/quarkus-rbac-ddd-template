package com.github.DaiYuANg.mobile.identity;

import com.github.DaiYuANg.audit.service.LoginLogService;
import com.github.DaiYuANg.modules.identity.application.LoginAuditEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class LoginAuditEventObserver {
  private final LoginLogService loginLogService;

  void onLoginAudit(@ObservesAsync @NonNull LoginAuditEvent event) {
    if (event.success()) {
      log.atDebug()
          .addKeyValue("username", event.username())
          .addKeyValue("remoteIp", event.remoteIp())
          .log("mobile login audit (success)");
      loginLogService.recordSuccess(event.username(), event.remoteIp(), event.userAgent());
    } else {
      log.atDebug()
          .addKeyValue("username", event.username())
          .addKeyValue("reason", event.reason())
          .log("mobile login audit (failure)");
      loginLogService.recordFailure(
          event.username(), event.remoteIp(), event.userAgent(), event.reason());
    }
  }
}
