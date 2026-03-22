package com.github.DaiYuANg.identity.view;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.github.DaiYuANg.identity.entity.SysUser;
import java.time.Instant;

/**
 * Blaze entity view aligned to the admin user list page.
 */
@EntityView(SysUser.class)
public interface UserListView {
    @IdMapping
    Long getId();
    String getUsername();
    String getNickname();
    String getEmail();
    String getMobilePhone();
    String getIdentifier();
    SysUserStatusView getUserStatus();
    Instant getLatestSignIn();

    interface SysUserStatusView {
        String name();
    }
}
