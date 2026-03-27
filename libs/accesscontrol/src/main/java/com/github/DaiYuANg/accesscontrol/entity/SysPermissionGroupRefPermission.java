package com.github.DaiYuANg.accesscontrol.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Join table entity for {@code sys_permission_group_ref_permission}.
 *
 * <p>Used for bulk operations without initializing {@code @ManyToMany} collections.
 *
 * @author ddddd &lt;dai_yuang@icloud.com&gt;
 */
@Entity
@Table(name = "sys_permission_group_ref_permission")
public class SysPermissionGroupRefPermission {

  @EmbeddedId public Id id;

  public SysPermissionGroupRefPermission() {}

  public SysPermissionGroupRefPermission(Long permissionGroupId, Long permissionId) {
    this.id = new Id(permissionGroupId, permissionId);
  }

  @Embeddable
  public static class Id implements Serializable {
    @Column(name = "permission_group_id", nullable = false)
    public Long permissionGroupId;

    @Column(name = "permission_id", nullable = false)
    public Long permissionId;

    public Id() {}

    public Id(Long permissionGroupId, Long permissionId) {
      this.permissionGroupId = permissionGroupId;
      this.permissionId = permissionId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Id id = (Id) o;
      return Objects.equals(permissionGroupId, id.permissionGroupId)
          && Objects.equals(permissionId, id.permissionId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(permissionGroupId, permissionId);
    }
  }
}
