package com.github.DaiYuANg.accesscontrol.view;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.github.DaiYuANg.accesscontrol.entity.SysPermission;

@EntityView(SysPermission.class)
public interface PermissionListView {
    @IdMapping
    Long getId();
    String getName();
    String getCode();
    String getResource();
    String getAction();
    String getGroupCode();
    String getDescription();
    String getExpression();
}
