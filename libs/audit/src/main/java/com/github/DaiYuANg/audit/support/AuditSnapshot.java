package com.github.DaiYuANg.audit.support;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record AuditSnapshot(
    String actorKey,
    String actorDisplayName,
    String actorType,
    String remoteIp,
    String userAgent,
    String requestId) {
  public static AuditSnapshot system() {
    return AuditSnapshotBuilder.builder()
        .actorKey("system")
        .actorDisplayName("system")
        .actorType("SYSTEM")
        .remoteIp(null)
        .userAgent(null)
        .requestId(null)
        .build();
  }
}
