package com.github.DaiYuANg.accesscontrol.query;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.parameter.RoleQuery;
import com.github.DaiYuANg.accesscontrol.projection.RoleListProjection;
import com.github.DaiYuANg.accesscontrol.view.RoleListView;
import com.github.DaiYuANg.persistence.query.BaseBlazeQueryRepository;
import com.github.DaiYuANg.persistence.query.PageSlice;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BlazeRoleQueryRepository extends BaseBlazeQueryRepository<SysRole, RoleQuery, RoleListProjection> implements RoleQueryRepository {
    @Inject EntityViewManager entityViewManager;
    @Inject MetamodelRoleQueryBuilder queryBuilder;

    @Override
    protected Class<SysRole> entityClass() {
        return SysRole.class;
    }

    @Override
    public PageSlice<RoleListProjection> page(RoleQuery query) {
        RoleQuerySpec spec = queryBuilder.build(query);
        var filter = spec.filter();
        CriteriaBuilder<SysRole> builder = newCriteriaBuilder("role");

        applyKeyword(builder, query.getKeyword());
        applyName(builder, filter.name());
        applyCode(builder, filter.exactCode());
        applySorts(builder, spec.sorts());

        EntityViewSetting<RoleListView, ?> setting = EntityViewSetting.create(RoleListView.class, query.offset(), query.getPageSize());
        var results = entityViewManager.applySetting(setting, builder).getResultList();
        return new PageSlice<>(results.stream().map(this::toProjection).toList(), results.size());
    }

    private void applyKeyword(CriteriaBuilder<SysRole> builder, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        var like = '%' + keyword.toLowerCase() + '%';
        builder.whereOr()
            .where("LOWER(name)").like().value(like).noEscape()
            .where("LOWER(code)").like().value(like).noEscape()
            .endOr();
    }

    private void applyName(CriteriaBuilder<SysRole> builder, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        builder.where("LOWER(name)").like().value('%' + name.toLowerCase() + '%').noEscape();
    }

    private void applyCode(CriteriaBuilder<SysRole> builder, String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        builder.where("code").eq(code);
    }

    private RoleListProjection toProjection(RoleListView view) {
        return new RoleListProjection(view.getId(), view.getName(), view.getCode(), view.getStatus() == null ? null : view.getStatus().name(), view.getSort());
    }
}
