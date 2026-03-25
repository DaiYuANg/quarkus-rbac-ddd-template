package com.github.DaiYuANg.modules.identity.application.dto.response;

import java.util.List;
import java.util.Set;

public record MeResponse(
    String id, String name, String email, List<MeRoleItem> roles, Set<String> permissions) {}
