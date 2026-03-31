package com.github.DaiYuANg.modules.accesscontrol.application.support;

import com.github.DaiYuANg.accesscontrol.repository.RoleRepository;
import com.github.DaiYuANg.cache.PermissionSnapshotStore;
import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserLifecycleSupport {
  private final RoleRepository roleRepository;
  private final PermissionSnapshotStore permissionSnapshotStore;
  private final RefreshTokenStore refreshTokenStore;

  public void assignRoles(@NonNull SysUser user, List<Long> roleIds) {
    user.roles.clear();
    if (roleIds == null) {
      return;
    }
    user.roles.addAll(roleRepository.findAllByIds(roleIds));
  }

  public void onPasswordChanged(@NonNull SysUser user) {
    invalidatePermissionSnapshot(user.id);
    revokeSessions(user);
  }

  public void onUserUpdated(
      @NonNull SysUser user, @NonNull String originalUsername, boolean usernameChanged) {
    invalidatePermissionSnapshot(user.id);
    if (usernameChanged) {
      revokeSessions(user.id, originalUsername);
    }
    if (user.userStatus == UserStatus.DISABLED) {
      revokeSessions(user);
    }
  }

  public void onUserDeleted(@NonNull SysUser user) {
    revokeSessions(user);
    invalidatePermissionSnapshot(user.id);
  }

  public void onRolesAssigned(@NonNull SysUser user) {
    invalidatePermissionSnapshot(user.id);
  }

  public void onStatusUpdated(@NonNull SysUser user) {
    invalidatePermissionSnapshot(user.id);
    if (user.userStatus == UserStatus.DISABLED) {
      revokeSessions(user);
    }
  }

  private void invalidatePermissionSnapshot(@NonNull Long userId) {
    permissionSnapshotStore.delete(userId);
  }

  private void revokeSessions(@NonNull SysUser user) {
    revokeSessions(user.id, user.username);
  }

  private void revokeSessions(@NonNull Long userId, String username) {
    refreshTokenStore.deleteByUserId(userId);
    refreshTokenStore.deleteByUsername(username);
  }
}
