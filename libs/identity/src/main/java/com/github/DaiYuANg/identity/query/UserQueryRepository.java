package com.github.DaiYuANg.identity.query;

import com.github.DaiYuANg.identity.parameter.UserQuery;
import com.github.DaiYuANg.identity.projection.UserListProjection;
import com.github.DaiYuANg.persistence.query.BaseQueryRepository;

/**
 * Query repository boundary reserved for Blaze-Persistence + static metamodel style implementation.
 */
public interface UserQueryRepository extends BaseQueryRepository<UserQuery, UserListProjection> {
}
