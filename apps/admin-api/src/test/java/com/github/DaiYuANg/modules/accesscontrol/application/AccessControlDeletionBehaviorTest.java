package com.github.DaiYuANg.modules.accesscontrol.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.accesscontrol.repository.PermissionGroupRepository;
import com.github.DaiYuANg.accesscontrol.repository.RoleRepository;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.PermissionGroupVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.PermissionVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.RoleVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.permissiongroup.PermissionGroupChecker;
import com.github.DaiYuANg.modules.accesscontrol.application.permissiongroup.PermissionGroupApplicationService;
import com.github.DaiYuANg.modules.accesscontrol.application.role.RoleChecker;
import com.github.DaiYuANg.modules.accesscontrol.application.role.RoleApplicationService;
import com.github.DaiYuANg.modules.accesscontrol.application.support.AccessControlAuditSupport;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AccessControlDeletionBehaviorTest {

  @Test
  void deleteRoleThrowsNotFoundWhenRoleMissing() {
    var roleRepository = mock(RoleRepository.class);
    var permissionGroupRepository = mock(PermissionGroupRepository.class);
    var permissionCatalogStore = mock(PermissionCatalogStore.class);
    var auditSupport = mock(AccessControlAuditSupport.class);
    var roleVOMapper = mock(RoleVOMapper.class);
    var permissionGroupVOMapper = mock(PermissionGroupVOMapper.class);
    var permissionVOMapper = mock(PermissionVOMapper.class);
    var roleChecker = mock(RoleChecker.class);
    var service =
        new RoleApplicationService(
            roleRepository,
            permissionGroupRepository,
            permissionCatalogStore,
            auditSupport,
            roleVOMapper,
            permissionGroupVOMapper,
            permissionVOMapper,
            roleChecker);
    when(roleRepository.findByIdOptional(7L)).thenReturn(Optional.empty());

    var ex = assertThrows(BizException.class, () -> service.deleteRole(7L));

    assertEquals(ResultCode.DATA_NOT_FOUND, ex.getResultCode());
    verify(roleRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    verify(auditSupport, never()).bumpGlobalVersion();
  }

  @Test
  void deletePermissionGroupThrowsNotFoundWhenGroupMissing() {
    var repository = mock(PermissionGroupRepository.class);
    var catalogStore = mock(PermissionCatalogStore.class);
    var entityManager = mock(EntityManager.class);
    var auditSupport = mock(AccessControlAuditSupport.class);
    var permissionGroupVOMapper = mock(PermissionGroupVOMapper.class);
    var permissionVOMapper = mock(PermissionVOMapper.class);
    var permissionGroupChecker = mock(PermissionGroupChecker.class);
    var service =
        new PermissionGroupApplicationService(
            repository,
            catalogStore,
            entityManager,
            auditSupport,
            permissionGroupVOMapper,
            permissionVOMapper,
            permissionGroupChecker);
    when(repository.findByIdOptional(9L)).thenReturn(Optional.empty());

    var ex = assertThrows(BizException.class, () -> service.deletePermissionGroup(9L));

    assertEquals(ResultCode.DATA_NOT_FOUND, ex.getResultCode());
    verify(repository, never()).delete(org.mockito.ArgumentMatchers.any());
    verify(auditSupport, never()).bumpGlobalVersion();
  }
}
