package com.liangdian.identity.query;

import com.liangdian.identity.parameter.UserQuery;
import com.liangdian.identity.projection.UserListProjection;
import com.liangdian.persistence.query.BaseQueryRepository;

/**
 * Query repository boundary reserved for Blaze-Persistence + static metamodel style implementation.
 */
public interface UserQueryRepository extends BaseQueryRepository<UserQuery, UserListProjection> {
}
