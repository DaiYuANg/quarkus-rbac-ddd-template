package com.liangdian.api.dto.response;

import java.time.Instant;
import java.util.Set;

public record PermissionGroupVO(Long id, String name, String description, String code, Integer sort, Instant createAt, Instant updateAt, Set<PermissionVO> permissions) {}
