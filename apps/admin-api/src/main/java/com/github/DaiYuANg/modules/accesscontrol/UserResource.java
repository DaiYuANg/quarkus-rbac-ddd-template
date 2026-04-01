package com.github.DaiYuANg.modules.accesscontrol;

import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.Auth;
import com.github.DaiYuANg.common.model.Results;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import com.google.common.primitives.Longs;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdateUserForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UserCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UserRefRoleForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.UserVO;
import com.github.DaiYuANg.modules.accesscontrol.application.user.UserApplicationService;
import com.github.DaiYuANg.modules.accesscontrol.query.UserPageQueryParams;
import io.quarkus.security.PermissionChecker;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.User;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.toolkit4j.data.model.envelope.Result;
import org.toolkit4j.data.model.page.PageResult;

@Path("/api/v1/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserResource {
  private final UserApplicationService userApplicationService;

  @GET
  @PermissionsAllowed(User.VIEW)
  public Result<String, PageResult<UserVO>> queryUserPage(
    @BeanParam @Valid UserPageQueryParams query) {
    return Results.ok(userApplicationService.queryUserPage(query.toQuery()));
  }

  @POST
  @PermissionsAllowed(User.ADD)
  public Result<String, UserVO> createUser(@Valid UserCreationForm form) {
    return Results.ok(userApplicationService.createUser(form));
  }

  @PUT
  @Path("/{id}/password")
  @PermissionsAllowed({Auth.CHANGE_PASSWORD, User.RESET_PASSWORD, User.EDIT})
  public Result<String, Void> updateUserPassword(
    @PathParam("id") Long id, @Valid ChangePasswordForm form) {
    userApplicationService.updateUserPassword(id, form.newPassword());
    return Results.ok();
  }

  @GET
  @Path("/{id}")
  @PermissionsAllowed(User.VIEW)
  public Result<String, Optional<UserVO>> getUserById(@PathParam("id") Long id) {
    return Results.ok(userApplicationService.getUserById(id));
  }

  @GET
  @Path("/list")
  @PermissionsAllowed(User.VIEW)
  public Result<String, List<UserVO>> getAllUsers() {
    return Results.ok(userApplicationService.getAllUsers());
  }

  @PUT
  @Path("/{id}")
  @PermissionsAllowed(User.EDIT)
  public Result<String, UserVO> updateUser(@PathParam("id") Long id, @Valid UpdateUserForm form) {
    return Results.ok(userApplicationService.updateUser(id, form));
  }

  @DELETE
  @Path("/{id}")
  @PermissionsAllowed(User.DELETE)
  public Result<String, Void> deleteUser(@PathParam("id") Long id) {
    userApplicationService.deleteUser(id);
    return Results.ok();
  }

  @GET
  @Path("/username/{username}")
  @PermissionsAllowed(User.VIEW)
  public Result<String, Optional<UserVO>> getUserByUsername(
    @PathParam("username") String username) {
    return Results.ok(userApplicationService.getUserByUsername(username));
  }

  @POST
  @Path("/assign/role")
  @PermissionsAllowed({User.EDIT, User.ASSIGN_ROLE})
  public Result<String, Void> assignRole(@Valid UserRefRoleForm form) {
    userApplicationService.assignRole(form);
    return Results.ok();
  }

  @GET
  @Path("/count/email/{email}")
  @PermissionsAllowed(User.VIEW)
  public Result<String, Long> countEmail(@PathParam("email") String email) {
    return Results.ok(userApplicationService.countEmail(email));
  }

  @GET
  @Path("/count/username/{username}")
  @PermissionsAllowed(User.VIEW)
  public Result<String, Long> countUsername(@PathParam("username") String username) {
    return Results.ok(userApplicationService.countUsername(username));
  }

  @GET
  @Path("/count/mobilePhone/{mobilePhone}")
  @PermissionsAllowed(User.VIEW)
  public Result<String, Long> countMobilePhone(@PathParam("mobilePhone") String mobilePhone) {
    return Results.ok(userApplicationService.countMobilePhone(mobilePhone));
  }

  @GET
  @Path("/count/identifier/{identifier}")
  @PermissionsAllowed(User.VIEW)
  public Result<String, Long> countIdentifier(@PathParam("identifier") String identifier) {
    return Results.ok(userApplicationService.countIdentifier(identifier));
  }

  @PUT
  @Path("/{id}/status")
  @PermissionsAllowed(User.EDIT)
  public Result<String, Void> updateUserStatus(
    @PathParam("id") Long id, @QueryParam("status") Integer status) {
    userApplicationService.updateUserStatus(id, status);
    return Results.ok();
  }

  @GET
  @Path("/count/userTotal")
  @PermissionsAllowed(User.VIEW)
  public Result<String, Long> countUserTotal() {
    return Results.ok(userApplicationService.countUserTotal());
  }

  @GET
  @Path("/count/userLoginTotal")
  @PermissionsAllowed(User.VIEW)
  public Result<String, Long> countUserLoginTotal() {
    return Results.ok(userApplicationService.countUserLoginTotal());
  }

  @PermissionChecker(Auth.CHANGE_PASSWORD)
  boolean canChangeOwnPassword(SecurityIdentity identity, Long id) {
    return Objects.equals(currentUserId(identity), id);
  }

  private Long currentUserId(SecurityIdentity identity) {
    if (identity == null) {
      return null;
    }
    val value = identity.getAttribute(PrincipalAttributeKeys.USER_ID);
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Longs.tryParse(StringUtils.trimToEmpty(Objects.toString(value, null)));
  }
}
