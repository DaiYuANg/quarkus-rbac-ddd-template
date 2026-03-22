package com.github.DaiYuANg.accesscontrol.query;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.github.DaiYuANg.accesscontrol.entity.SysPermission;
import com.github.DaiYuANg.accesscontrol.parameter.PermissionQuery;
import com.github.DaiYuANg.accesscontrol.projection.PermissionListProjection;
import com.github.DaiYuANg.accesscontrol.view.PermissionListView;
import com.github.DaiYuANg.persistence.query.BaseBlazeQueryRepository;
import com.github.DaiYuANg.persistence.query.PageSlice;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BlazePermissionQueryRepository extends BaseBlazeQueryRepository<SysPermission, PermissionQuery, PermissionListProjection> implements PermissionQueryRepository {
    @Inject EntityViewManager entityViewManager;
    @Inject MetamodelPermissionQueryBuilder queryBuilder;

    @Override
    protected Class<SysPermission> entityClass() {
        return SysPermission.class;
    }

    @Override
    public PageSlice<PermissionListProjection> page(PermissionQuery query) {
        PermissionQuerySpec spec = queryBuilder.build(query);
        var filter = spec.filter();
        CriteriaBuilder<SysPermission> builder = newCriteriaBuilder("permission");

        applyKeyword(builder, query.getKeyword());
        applyLike(builder, "name", filter.name());
        applyLike(builder, "code", filter.code());
        applyEquals(builder, "domain", filter.domain());
        applyEquals(builder, "resource", filter.resource());
        applyLike(builder, "action", filter.action());
        applyEquals(builder, "groupCode", filter.groupCode());
        applySorts(builder, spec.sorts());

        EntityViewSetting<PermissionListView, ?> setting = EntityViewSetting.create(PermissionListView.class, query.offset(), query.getPageSize());
        var results = entityViewManager.applySetting(setting, builder).getResultList();
        return new PageSlice<>(results.stream().map(this::toProjection).toList(), results.size());
    }

    private void applyKeyword(CriteriaBuilder<SysPermission> builder, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        var like = '%' + keyword.toLowerCase() + '%';
        builder.whereOr()
            .where("LOWER(name)").like().value(like).noEscape()
            .where("LOWER(code)").like().value(like).noEscape()
            .where("LOWER(domain)").like().value(like).noEscape()
            .where("LOWER(resource)").like().value(like).noEscape()
            .where("LOWER(action)").like().value(like).noEscape()
            .where("LOWER(groupCode)").like().value(like).noEscape()
            .where("LOWER(description)").like().value(like).noEscape()
            .endOr();
    }

    private void applyLike(CriteriaBuilder<SysPermission> builder, String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        builder.where("LOWER(" + field + ")").like().value('%' + value.toLowerCase() + '%').noEscape();
    }

    private void applyEquals(CriteriaBuilder<SysPermission> builder, String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        builder.where(field).eq(value);
    }

    private PermissionListProjection toProjection(PermissionListView view) {
        return new PermissionListProjection(
            view.getId(),
            view.getName(),
            view.getCode(),
            view.getDomain(),
            view.getResource(),
            view.getAction(),
            view.getGroupCode(),
            view.getDescription(),
            view.getExpression()
        );
    }
}
