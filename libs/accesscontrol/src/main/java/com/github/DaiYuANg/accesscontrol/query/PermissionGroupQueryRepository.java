package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.parameter.PermissionGroupQuery;
import com.github.DaiYuANg.accesscontrol.projection.PermissionGroupListProjection;
import com.github.DaiYuANg.persistence.query.BaseQueryRepository;

public interface PermissionGroupQueryRepository
    extends BaseQueryRepository<PermissionGroupQuery, PermissionGroupListProjection> {}
