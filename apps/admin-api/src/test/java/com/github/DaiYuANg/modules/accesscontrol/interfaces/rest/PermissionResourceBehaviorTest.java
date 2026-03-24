package com.github.DaiYuANg.modules.accesscontrol.interfaces.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.DaiYuANg.modules.accesscontrol.application.permission.PermissionApplicationService;
import com.github.DaiYuANg.modules.accesscontrol.application.permissiongroup.PermissionGroupApplicationService;
import com.github.DaiYuANg.modules.accesscontrol.interfaces.rest.dto.PermissionGroupBindingForm;
import org.junit.jupiter.api.Test;

class PermissionResourceBehaviorTest {

  @Test
  void bindGroupBulkParsesCsvIdsAndDelegates() {
    var permissionApplicationService = mock(PermissionApplicationService.class);
    var permissionGroupApplicationService = mock(PermissionGroupApplicationService.class);
    var resource =
        new PermissionResource(permissionApplicationService, permissionGroupApplicationService);

    resource.bindGroupBulk("1, 2,3", new PermissionGroupBindingForm(9L));

    verify(permissionGroupApplicationService).bindPermissionsToGroup(9L, java.util.List.of(1L, 2L, 3L));
  }

  @Test
  void bindGroupSingleDelegatesWithSingleIdList() {
    var permissionApplicationService = mock(PermissionApplicationService.class);
    var permissionGroupApplicationService = mock(PermissionGroupApplicationService.class);
    var resource =
        new PermissionResource(permissionApplicationService, permissionGroupApplicationService);

    resource.bindGroup(7L, new PermissionGroupBindingForm(null));

    verify(permissionGroupApplicationService).bindPermissionsToGroup(null, java.util.List.of(7L));
  }
}
