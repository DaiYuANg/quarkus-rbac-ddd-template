package com.liangdian.identity.query;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.liangdian.identity.constant.UserStatus;
import com.liangdian.identity.entity.SysUser;
import com.liangdian.identity.parameter.UserQuery;
import com.liangdian.identity.projection.UserListProjection;
import com.liangdian.identity.view.UserListView;
import com.liangdian.persistence.query.BaseBlazeQueryRepository;
import com.liangdian.persistence.query.PageSlice;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@DefaultBean
@ApplicationScoped
public class BlazeUserQueryRepository extends BaseBlazeQueryRepository<SysUser, UserQuery, UserListProjection> implements UserQueryRepository {
    @Inject EntityViewManager entityViewManager;
    @Inject MetamodelUserQueryBuilder queryBuilder;

    @Override
    protected Class<SysUser> entityClass() {
        return SysUser.class;
    }

    @Override
    public PageSlice<UserListProjection> page(UserQuery query) {
        UserQuerySpec spec = queryBuilder.build(query);
        var filter = spec.filter();
        CriteriaBuilder<SysUser> builder = newCriteriaBuilder("user");

        applyKeyword(builder, query.getKeyword());
        applyUsername(builder, filter.username());
        applyStatus(builder, filter.userStatus());
        applySorts(builder, spec.sorts());

        EntityViewSetting<UserListView, ?> setting = EntityViewSetting.create(UserListView.class, query.offset(), query.getPageSize());
        var paged = (PagedList<UserListView>) entityViewManager.applySetting(setting, builder).getResultList();
        return new PageSlice<>(paged.stream().map(this::toProjection).toList(), paged.getTotalSize());
    }

    private void applyKeyword(CriteriaBuilder<SysUser> builder, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        var like = '%' + keyword.toLowerCase() + '%';
        builder.whereOr()
            .where("LOWER(username)").like().value(like).noEscape()
            .where("LOWER(nickname)").like().value(like).noEscape()
            .where("LOWER(email)").like().value(like).noEscape()
            .endOr();
    }

    private void applyUsername(CriteriaBuilder<SysUser> builder, String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        builder.where("LOWER(username)").like().value('%' + username.toLowerCase() + '%').noEscape();
    }

    private void applyStatus(CriteriaBuilder<SysUser> builder, UserStatus userStatus) {
        if (userStatus == null) {
            return;
        }
        builder.where("userStatus").eq(userStatus);
    }

    private UserListProjection toProjection(UserListView view) {
        return new UserListProjection(view.getId(), view.getUsername(), view.getNickname(), view.getEmail(), view.getMobilePhone(), view.getIdentifier(), view.getUserStatus() == null ? null : view.getUserStatus().name(), view.getLatestSignIn());
    }
}
