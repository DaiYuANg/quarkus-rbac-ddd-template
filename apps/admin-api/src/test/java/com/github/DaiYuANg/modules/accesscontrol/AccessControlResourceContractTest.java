package com.github.DaiYuANg.modules.accesscontrol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.PermissionGroup;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.Role;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.User;
import io.quarkus.security.PermissionsAllowed;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class AccessControlResourceContractTest {

  @Test
  void userCountEndpointsRequireViewPermission() throws Exception {
    assertPermission(UserResource.class, "countEmail", User.VIEW, String.class);
    assertPermission(UserResource.class, "countUsername", User.VIEW, String.class);
    assertPermission(UserResource.class, "countMobilePhone", User.VIEW, String.class);
    assertPermission(UserResource.class, "countIdentifier", User.VIEW, String.class);
    assertPermission(UserResource.class, "countUserTotal", User.VIEW);
    assertPermission(UserResource.class, "countUserLoginTotal", User.VIEW);
  }

  @Test
  void roleCountEndpointsRequireViewPermission() throws Exception {
    assertPermission(RoleResource.class, "countCode", Role.VIEW, String.class);
    assertPermission(RoleResource.class, "countRoleTotal", Role.VIEW);
  }

  @Test
  void permissionGroupCountEndpointsRequireViewPermission() throws Exception {
    assertPermission(PermissionGroupResource.class, "countName", PermissionGroup.VIEW, String.class);
    assertPermission(PermissionGroupResource.class, "count", PermissionGroup.VIEW);
  }

  private void assertPermission(
      Class<?> resourceType, String methodName, String expectedPermission, Class<?>... parameterTypes)
      throws Exception {
    Method method = resourceType.getDeclaredMethod(methodName, parameterTypes);
    PermissionsAllowed permissionsAllowed = method.getAnnotation(PermissionsAllowed.class);
    assertNotNull(permissionsAllowed);
    assertArrayEquals(new String[] {expectedPermission}, permissionsAllowed.value());
  }
}
