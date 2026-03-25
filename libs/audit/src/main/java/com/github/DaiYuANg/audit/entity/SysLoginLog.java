package com.github.DaiYuANg.audit.entity;

import com.github.DaiYuANg.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "sys_login_log")
public class SysLoginLog extends BaseEntity {
  @Column(name = "username", length = 128)
  public String username;

  @Column(name = "success", nullable = false)
  public Boolean success;

  @Column(name = "reason", length = 255)
  public String reason;

  @Column(name = "remote_ip", length = 128)
  public String remoteIp;

  @Column(name = "user_agent", length = 512)
  public String userAgent;

  @Column(name = "login_at", nullable = false)
  public Instant loginAt;
}
