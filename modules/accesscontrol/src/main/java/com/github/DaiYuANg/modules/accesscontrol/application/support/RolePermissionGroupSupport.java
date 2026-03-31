package com.github.DaiYuANg.modules.accesscontrol.application.support;

import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.repository.PermissionGroupRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RolePermissionGroupSupport {
  private final PermissionGroupRepository permissionGroupRepository;

  public void assignPermissionGroups(@NonNull SysRole role, List<Long> permissionGroupIds) {
    role.permissionGroups.clear();
    if (permissionGroupIds == null) {
      return;
    }
    role.permissionGroups.addAll(permissionGroupRepository.findAllByIds(permissionGroupIds));
  }
}
