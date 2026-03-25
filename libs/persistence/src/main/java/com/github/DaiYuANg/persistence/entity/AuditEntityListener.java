package com.github.DaiYuANg.persistence.entity;

import com.github.DaiYuANg.security.audit.ActorAuditor;
import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import org.jspecify.annotations.NonNull;

public class AuditEntityListener {
  @Inject ActorAuditor actorAuditor;

  @PrePersist
  public void prePersist(@NonNull BaseEntity entity) {
    var now = Instant.now();
    entity.createAt = now;
    entity.updateAt = now;
    var actor = actorAuditor.currentActorKey();
    entity.createBy = actor;
    entity.updateBy = actor;
  }

  @PreUpdate
  public void preUpdate(@NonNull BaseEntity entity) {
    entity.updateAt = Instant.now();
    entity.updateBy = actorAuditor.currentActorKey();
  }
}
