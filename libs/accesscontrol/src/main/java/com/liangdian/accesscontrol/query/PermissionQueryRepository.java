package com.liangdian.accesscontrol.query;

import com.liangdian.accesscontrol.parameter.PermissionQuery;
import com.liangdian.accesscontrol.projection.PermissionListProjection;
import com.liangdian.persistence.query.BaseQueryRepository;

public interface PermissionQueryRepository extends BaseQueryRepository<PermissionQuery, PermissionListProjection> {
}
