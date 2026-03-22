package com.liangdian.persistence.query;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import jakarta.inject.Inject;
import java.util.function.Function;

public abstract class BaseEntityViewQueryRepository<E, Q, V, EV> extends BaseBlazeQueryRepository<E, Q, V> {
    @Inject protected EntityViewManager entityViewManager;

    protected abstract Class<EV> entityViewClass();

    protected PagedList<EV> fetchPage(CriteriaBuilder<E> builder, int firstResult, int maxResults) {
        var setting = EntityViewSetting.create(entityViewClass(), firstResult, maxResults);
        return entityViewManager.applySetting(setting, builder).getResultList();
    }

    protected <T> PageSlice<V> pageEntityViews(PagedList<T> page, Function<T, V> mapper) {
        return page(page, mapper);
    }
}
