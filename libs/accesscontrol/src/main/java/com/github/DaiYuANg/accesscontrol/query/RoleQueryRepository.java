package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.projection.RoleListProjection;
import com.github.DaiYuANg.persistence.query.BaseQueryRepository;

/**
 * Query repository boundary reserved for Blaze-Persistence + static metamodel style implementation.
 */
public interface RoleQueryRepository
    extends BaseQueryRepository<RolePageQuery, RoleListProjection> {}
