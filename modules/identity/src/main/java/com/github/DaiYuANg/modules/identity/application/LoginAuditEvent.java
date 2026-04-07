package com.github.DaiYuANg.modules.identity.application;

import io.soabase.recordbuilder.core.RecordBuilder;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

/**
 * Event for asynchronous login audit logging (success or failure). Carries request metadata
 * captured in the request thread before async dispatch.
 */
@RecordBuilder
public record LoginAuditEvent(
    String username, boolean success, String reason, String remoteIp, String userAgent) {
  @Contract("_, _, _ -> new")
  public static @NonNull LoginAuditEvent success(String username, String remoteIp, String userAgent) {
    return new LoginAuditEvent(username, true, null, remoteIp, userAgent);
  }

  @Contract("_, _, _, _ -> new")
  public static @NonNull LoginAuditEvent failure(
      String username, String reason, String remoteIp, String userAgent) {
    return new LoginAuditEvent(username, false, reason, remoteIp, userAgent);
  }
}
