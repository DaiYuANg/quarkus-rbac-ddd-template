package com.github.DaiYuANg.audit.support;

import com.github.DaiYuANg.security.access.RequestMetadataAccess;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUserProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuditSnapshotProvider {
  private final CurrentAuthenticatedUserProvider currentAuthenticatedUserProvider;
  private final RequestMetadataAccess requestMetadataAccess;

  public AuditSnapshot snapshot() {
    val request = requestMetadataAccess.current();
    val user = currentAuthenticatedUserProvider.getCurrentUser().orElse(null);
    if (user == null) {
      return AuditSnapshotBuilder.builder()
          .actorKey("SYSTEM:anonymous")
          .actorDisplayName("anonymous")
          .actorType("SYSTEM")
          .remoteIp(request.remoteIp())
          .userAgent(request.userAgent())
          .requestId(request.requestId())
          .build();
    }
    return AuditSnapshotBuilder.builder()
        .actorKey(user.userType() + ":" + user.username())
        .actorDisplayName(user.displayName())
        .actorType(user.userType())
        .remoteIp(request.remoteIp())
        .userAgent(request.userAgent())
        .requestId(request.requestId())
        .build();
  }
}
