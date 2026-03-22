package com.github.DaiYuANg.identity.entity;

import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "sys_user")
public class SysUser extends BaseEntity {
    @Column(nullable = false, unique = true, length = 128)
    public String username;

    @Column(nullable = false, length = 255)
    public String password;

    @Column(nullable = false, unique = true, length = 128)
    public String identifier = UUID.randomUUID().toString();

    @Column(length = 64)
    public String mobilePhone;

    @Column(length = 128)
    public String nickname;

    @Column(length = 128)
    public String email;

    @Column(name = "latest_sign_in")
    public Instant latestSignIn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    public UserStatus userStatus = UserStatus.ENABLED;

    @ManyToMany
    @JoinTable(name = "sys_user_ref_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    public Set<SysRole> roles = new LinkedHashSet<>();
}
