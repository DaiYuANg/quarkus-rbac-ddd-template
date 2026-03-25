package com.github.DaiYuANg.audit.entity;

import com.github.DaiYuANg.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "sys_operation_log")
public class SysOperationLog extends BaseEntity {
  @Column(name = "operator", length = 128)
  public String operator;

  @Column(name = "operator_display_name", length = 255)
  public String operatorDisplayName;

  @Column(name = "operator_type", length = 64)
  public String operatorType;

  @Column(name = "module", length = 128)
  public String module;

  @Column(name = "action", length = 128)
  public String action;

  @Column(name = "target", length = 255)
  public String target;

  @Column(name = "success", nullable = false)
  public Boolean success;

  @Column(name = "detail", length = 2000)
  public String detail;

  @Column(name = "remote_ip", length = 128)
  public String remoteIp;

  @Column(name = "user_agent", length = 512)
  public String userAgent;

  @Column(name = "request_id", length = 128)
  public String requestId;

  @Column(name = "occurred_at", nullable = false)
  public Instant occurredAt;
}
