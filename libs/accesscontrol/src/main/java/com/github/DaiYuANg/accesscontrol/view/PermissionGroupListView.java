package com.github.DaiYuANg.accesscontrol.view;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;

@EntityView(SysPermissionGroup.class)
public interface PermissionGroupListView {
    @IdMapping
    Long getId();
    String getName();
    String getDescription();
    String getCode();
    Integer getSort();
}
