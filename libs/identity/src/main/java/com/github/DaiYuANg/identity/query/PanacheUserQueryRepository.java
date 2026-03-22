package com.github.DaiYuANg.identity.query;

import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.parameter.UserQuery;
import com.github.DaiYuANg.identity.projection.UserListProjection;
import com.github.DaiYuANg.persistence.query.PageSlice;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Panache fallback kept as a safe default when the Blaze-based repository is
 * replaced or disabled. In normal runs the dedicated Blaze implementation wins.
 */
@ApplicationScoped
public class PanacheUserQueryRepository implements UserQueryRepository {
    @Override
    public PageSlice<UserListProjection> page(UserQuery query) {
        var jpql = new StringBuilder("from SysUser u where 1=1");
        var params = new java.util.HashMap<String, Object>();

        if (query.getKeyword() != null) {
            jpql.append(" and (u.username like :keyword or u.nickname like :keyword or u.email like :keyword)");
            params.put("keyword", '%' + query.getKeyword() + '%');
        }
        if (query.getUsername() != null && !query.getUsername().isBlank()) {
            jpql.append(" and u.username like :username");
            params.put("username", '%' + query.getUsername().trim() + '%');
        }
        if (query.getUserStatus() != null) {
            jpql.append(" and u.userStatus = :userStatus");
            params.put("userStatus", query.getUserStatus());
        }
        jpql.append(" order by u.id desc");

        PanacheQuery<SysUser> panacheQuery = SysUser.find(jpql.toString(), params)
            .page(query.getPage(), query.getSize());

        var content = panacheQuery.list().stream()
            .map(user -> new UserListProjection(
                user.id,
                user.username,
                user.nickname,
                user.email,
                user.mobilePhone,
                user.identifier,
                user.userStatus == null ? null : user.userStatus.name(),
                user.latestSignIn
            ))
            .toList();

        return new PageSlice<>(content, panacheQuery.count());
    }
}
