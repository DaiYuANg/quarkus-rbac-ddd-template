package com.github.DaiYuANg.accesscontrol.view;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.github.DaiYuANg.accesscontrol.entity.SysRole;

/**
 * Blaze entity view aligned to the admin role list page.
 */
@EntityView(SysRole.class)
public interface RoleListView {
    @IdMapping
    Long getId();
    String getName();
    String getCode();
    SysRoleStatusView getStatus();
    Integer getSort();

    interface SysRoleStatusView {
        String name();
    }
}
