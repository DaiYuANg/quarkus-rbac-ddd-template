package com.github.DaiYuANg.application.auth;

/**
 * Event for asynchronous login audit logging (success or failure).
 * Carries request metadata captured in the request thread before async dispatch.
 */
public record LoginAuditEvent(
    String username,
    boolean success,
    String reason,
    String remoteIp,
    String userAgent
) {
  public static LoginAuditEvent success(String username, String remoteIp, String userAgent) {
    return new LoginAuditEvent(username, true, null, remoteIp, userAgent);
  }

  public static LoginAuditEvent failure(String username, String reason, String remoteIp, String userAgent) {
    return new LoginAuditEvent(username, false, reason, remoteIp, userAgent);
  }
}
