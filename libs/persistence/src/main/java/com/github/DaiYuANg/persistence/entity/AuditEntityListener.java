package com.github.DaiYuANg.persistence.entity;

import com.github.DaiYuANg.security.ActorAuditor;
import io.quarkus.arc.Arc;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import java.util.Optional;

public class AuditEntityListener {
    @PrePersist
    public void prePersist(BaseEntity entity) {
        var now = Instant.now();
        entity.createAt = now;
        entity.updateAt = now;
        currentActorKey().ifPresent(actor -> {
            entity.createBy = actor;
            entity.updateBy = actor;
        });
    }

    @PreUpdate
    public void preUpdate(BaseEntity entity) {
        entity.updateAt = Instant.now();
        currentActorKey().ifPresent(actor -> entity.updateBy = actor);
    }

    private Optional<String> currentActorKey() {
        var instance = Arc.container().instance(ActorAuditor.class);
        if (instance == null || !instance.isAvailable()) {
            return Optional.empty();
        }
        return Optional.ofNullable(instance.get().currentActorKey());
    }
}
