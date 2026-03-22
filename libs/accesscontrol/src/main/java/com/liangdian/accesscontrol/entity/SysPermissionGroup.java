package com.liangdian.accesscontrol.entity;

import com.liangdian.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "sys_permission_group")
public class SysPermissionGroup extends BaseEntity {
    @Column(nullable = false, unique = true, length = 128)
    public String name;

    @Column(length = 255)
    public String description;

    @Column(nullable = false, unique = true, length = 128)
    public String code;

    @Column
    public Integer sort = 0;

    @ManyToMany
    @JoinTable(name = "sys_permission_group_ref_permission",
        joinColumns = @JoinColumn(name = "permission_group_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"))
    public Set<SysPermission> permissions = new LinkedHashSet<>();
}
