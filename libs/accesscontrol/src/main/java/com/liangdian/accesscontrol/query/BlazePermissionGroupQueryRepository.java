package com.liangdian.accesscontrol.query;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.liangdian.accesscontrol.entity.SysPermissionGroup;
import com.liangdian.accesscontrol.parameter.PermissionGroupQuery;
import com.liangdian.accesscontrol.projection.PermissionGroupListProjection;
import com.liangdian.accesscontrol.view.PermissionGroupListView;
import com.liangdian.persistence.query.BaseBlazeQueryRepository;
import com.liangdian.persistence.query.PageSlice;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BlazePermissionGroupQueryRepository extends BaseBlazeQueryRepository<SysPermissionGroup, PermissionGroupQuery, PermissionGroupListProjection> implements PermissionGroupQueryRepository {
    @Inject EntityViewManager entityViewManager;
    @Inject MetamodelPermissionGroupQueryBuilder queryBuilder;

    @Override
    protected Class<SysPermissionGroup> entityClass() {
        return SysPermissionGroup.class;
    }

    @Override
    public PageSlice<PermissionGroupListProjection> page(PermissionGroupQuery query) {
        PermissionGroupQuerySpec spec = queryBuilder.build(query);
        var filter = spec.filter();
        CriteriaBuilder<SysPermissionGroup> builder = newCriteriaBuilder("permissionGroup");

        applyKeyword(builder, query.getKeyword());
        applyName(builder, filter.name());
        applySorts(builder, spec.sorts());

        EntityViewSetting<PermissionGroupListView, ?> setting = EntityViewSetting.create(PermissionGroupListView.class, query.offset(), query.getPageSize());
        var results = entityViewManager.applySetting(setting, builder).getResultList();
        return new PageSlice<>(results.stream().map(this::toProjection).toList(), results.size());
    }

    private void applyKeyword(CriteriaBuilder<SysPermissionGroup> builder, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        var like = '%' + keyword.toLowerCase() + '%';
        builder.whereOr()
            .where("LOWER(name)").like().value(like).noEscape()
            .where("LOWER(code)").like().value(like).noEscape()
            .endOr();
    }

    private void applyName(CriteriaBuilder<SysPermissionGroup> builder, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        builder.where("LOWER(name)").like().value('%' + name.toLowerCase() + '%').noEscape();
    }

    private PermissionGroupListProjection toProjection(PermissionGroupListView view) {
        return new PermissionGroupListProjection(view.getId(), view.getName(), view.getDescription(), view.getCode(), view.getSort());
    }
}
