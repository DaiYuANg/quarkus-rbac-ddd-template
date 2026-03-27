package com.github.DaiYuANg.security.authorization;

/**
 * Canonical RBAC permission codes ({@code resource:action}) for admin APIs. Use these instead of
 * duplicating string literals in {@code @PermissionsAllowed}, {@link AuthorizationService} checks,
 * and tests.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
public final class RbacPermissionCodes {

  private RbacPermissionCodes() {}

  /** Permissions on the {@code user} resource. */
  public static final class User {
    private User() {}

    public static final String VIEW = "user:view";
    public static final String ADD = "user:add";
    public static final String EDIT = "user:edit";
    public static final String DELETE = "user:delete";
    public static final String ASSIGN_ROLE = "user:assign-role";
    public static final String RESET_PASSWORD = "user:reset-password";
  }

  /** Permissions on the {@code role} resource. */
  public static final class Role {
    private Role() {}

    public static final String VIEW = "role:view";
    public static final String ADD = "role:add";
    public static final String EDIT = "role:edit";
    public static final String DELETE = "role:delete";
    public static final String ASSIGN_PERMISSION_GROUP = "role:assign-permission-group";
  }

  /** Permissions on the {@code permission} resource. */
  public static final class Permission {
    private Permission() {}

    public static final String VIEW = "permission:view";
    public static final String EDIT = "permission:edit";
  }

  /** Permissions on the {@code permission-group} resource. */
  public static final class PermissionGroup {
    private PermissionGroup() {}

    public static final String VIEW = "permission-group:view";
    public static final String ADD = "permission-group:add";
    public static final String EDIT = "permission-group:edit";
    public static final String DELETE = "permission-group:delete";
    public static final String ASSIGN_PERMISSION = "permission-group:assign-permission";
  }

  /** Permissions on the {@code auth} resource. */
  public static final class Auth {
    private Auth() {}

    public static final String CHANGE_PASSWORD = "auth:change-password";
  }
}
