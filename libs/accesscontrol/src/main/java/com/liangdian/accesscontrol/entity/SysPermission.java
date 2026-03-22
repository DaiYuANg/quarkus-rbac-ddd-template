package com.liangdian.accesscontrol.entity;

import com.liangdian.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "sys_permission")
public class SysPermission extends BaseEntity {
    @Column(nullable = false, unique = true, length = 128)
    public String name;

    @Column(nullable = false, unique = true, length = 128)
    public String code;

    @Column(nullable = false, length = 64)
    public String domain;

    @Column(nullable = false, length = 128)
    public String resource;

    @Column(nullable = false, length = 128)
    public String action;

    @Column(name = "group_code", length = 128)
    public String groupCode;

    @Column(length = 255)
    public String description;

    @Column(length = 255)
    public String expression;
}
