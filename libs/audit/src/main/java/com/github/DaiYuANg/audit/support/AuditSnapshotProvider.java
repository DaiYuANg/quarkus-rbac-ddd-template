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
      return new AuditSnapshot(
          "SYSTEM:anonymous",
          "anonymous",
          "SYSTEM",
          request.remoteIp(),
          request.userAgent(),
          request.requestId());
    }
    return new AuditSnapshot(
        user.userType() + ":" + user.username(),
        user.displayName(),
        user.userType(),
        request.remoteIp(),
        request.userAgent(),
        request.requestId());
  }
}
