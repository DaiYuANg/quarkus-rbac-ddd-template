package com.github.DaiYuANg.modules.accesscontrol.application.support;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record AccessControlAuditCommand(
    String module, String action, String target, boolean success, String detail) {}
