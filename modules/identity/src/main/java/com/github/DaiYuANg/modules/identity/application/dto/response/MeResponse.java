package com.github.DaiYuANg.modules.identity.application.dto.response;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;
import java.util.Set;

@RecordBuilder
public record MeResponse(
  String id, String name, String email, List<MeRoleItem> roles, Set<String> permissions) {
}
