package com.liangdian.accesscontrol.entity;

import com.liangdian.accesscontrol.constant.RoleStatus;
import com.liangdian.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "sys_role")
public class SysRole extends BaseEntity {
    @Column(nullable = false, unique = true, length = 128)
    public String name;

    @Column(nullable = false, unique = true, length = 128)
    public String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    public RoleStatus status = RoleStatus.ENABLED;

    @Column
    public Integer sort = 0;

    @Column(length = 255)
    public String description;

    @ManyToMany
    @JoinTable(name = "sys_role_ref_permission_group",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_group_id"))
    public Set<SysPermissionGroup> permissionGroups = new LinkedHashSet<>();
}
