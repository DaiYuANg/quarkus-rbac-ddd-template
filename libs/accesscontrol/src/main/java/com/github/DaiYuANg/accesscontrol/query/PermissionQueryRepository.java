package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.projection.PermissionListProjection;
import com.github.DaiYuANg.persistence.query.BaseQueryRepository;

public interface PermissionQueryRepository
    extends BaseQueryRepository<PermissionPageQuery, PermissionListProjection> {}
