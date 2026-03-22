package com.liangdian.audit.support;

public record AuditSnapshot(
    String actorKey,
    String actorDisplayName,
    String actorType,
    String remoteIp,
    String userAgent,
    String requestId
) {
    public static AuditSnapshot system() {
        return new AuditSnapshot("system", "system", "SYSTEM", null, null, null);
    }
}
