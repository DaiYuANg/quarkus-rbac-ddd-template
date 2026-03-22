package com.liangdian.accesscontrol.query;

import com.liangdian.accesscontrol.parameter.RoleQuery;
import com.liangdian.accesscontrol.projection.RoleListProjection;
import com.liangdian.persistence.query.BaseQueryRepository;

/**
 * Query repository boundary reserved for Blaze-Persistence + static metamodel style implementation.
 */
public interface RoleQueryRepository extends BaseQueryRepository<RoleQuery, RoleListProjection> {
}
