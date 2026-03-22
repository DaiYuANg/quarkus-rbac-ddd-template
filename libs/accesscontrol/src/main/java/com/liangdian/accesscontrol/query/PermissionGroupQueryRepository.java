package com.liangdian.accesscontrol.query;

import com.liangdian.accesscontrol.parameter.PermissionGroupQuery;
import com.liangdian.accesscontrol.projection.PermissionGroupListProjection;
import com.liangdian.persistence.query.BaseQueryRepository;

public interface PermissionGroupQueryRepository extends BaseQueryRepository<PermissionGroupQuery, PermissionGroupListProjection> {
}
