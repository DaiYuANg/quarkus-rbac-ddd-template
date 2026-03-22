package com.liangdian.accesscontrol.query;

import com.liangdian.accesscontrol.entity.SysRole;
import com.liangdian.accesscontrol.parameter.RoleQuery;
import com.liangdian.accesscontrol.projection.RoleListProjection;
import com.liangdian.persistence.query.PageSlice;
import io.quarkus.arc.DefaultBean;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Panache fallback kept as a safe default when the Blaze-based repository is
 * replaced or disabled. In normal runs the dedicated Blaze implementation wins.
 */
@DefaultBean
@ApplicationScoped
public class PanacheRoleQueryRepository implements RoleQueryRepository {
    @Override
    public PageSlice<RoleListProjection> page(RoleQuery query) {
        var jpql = new StringBuilder("from SysRole r where 1=1");
        var params = new java.util.HashMap<String, Object>();

        if (query.getKeyword() != null) {
            jpql.append(" and (r.name like :keyword or r.code like :keyword)");
            params.put("keyword", '%' + query.getKeyword() + '%');
        }
        if (query.getName() != null && !query.getName().isBlank()) {
            jpql.append(" and r.name like :name");
            params.put("name", '%' + query.getName().trim() + '%');
        }
        jpql.append(" order by r.sort asc, r.id desc");

        PanacheQuery<SysRole> panacheQuery = SysRole.find(jpql.toString(), params)
            .page(query.getPage(), query.getSize());

        var content = panacheQuery.list().stream()
            .map(role -> new RoleListProjection(
                role.id,
                role.name,
                role.code,
                role.status == null ? null : role.status.name(),
                role.sort
            ))
            .toList();

        return new PageSlice<>(content, panacheQuery.count());
    }
}
