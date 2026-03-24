package com.github.DaiYuANg.persistence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import org.toolkit4j.integration.hibernate.snowflake.id.SnowflakeGenerator;

@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
public abstract class BaseEntity extends PanacheEntityBase {
    @Id
    @SnowflakeGenerator
    public Long id;

    @Column(name = "create_at", nullable = false)
    public Instant createAt;

    @Column(name = "update_at", nullable = false)
    public Instant updateAt;

    @Column(name = "create_by", length = 128)
    public String createBy;

    @Column(name = "update_by", length = 128)
    public String updateBy;
}
