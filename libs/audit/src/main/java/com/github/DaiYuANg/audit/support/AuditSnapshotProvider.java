package com.github.DaiYuANg.audit.support;

import com.github.DaiYuANg.security.access.CurrentUserAccess;
import com.github.DaiYuANg.security.request.RequestMetadata;
import com.github.DaiYuANg.security.request.RequestMetadataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuditSnapshotProvider {
  private final CurrentUserAccess currentUserAccess;
  private final Instance<RequestMetadataProvider> requestMetadataProvider;

  public AuditSnapshot snapshot() {
    val request = currentRequest();
    val user = currentUserAccess.currentUser().orElse(null);
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

  private RequestMetadata currentRequest() {
    if (requestMetadataProvider.isUnsatisfied()) {
      return RequestMetadata.empty();
    }
    return requestMetadataProvider.get().currentOrEmpty();
  }
}
