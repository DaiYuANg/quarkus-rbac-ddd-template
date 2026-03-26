package com.github.DaiYuANg.modules.accesscontrol;

import com.github.DaiYuANg.accesscontrol.parameter.RoleQuery;
import com.github.DaiYuANg.common.model.ApiPageResult;
import com.github.DaiYuANg.common.model.Results;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.RoleCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.RoleRefPermissionGroupForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdateRoleForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.RoleVO;
import com.github.DaiYuANg.modules.accesscontrol.application.role.RoleApplicationService;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.Role;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.toolkit4j.data.model.envelope.Result;

@Path("/api/v1/role")
@Produces("application/json")
@Consumes("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RoleResource {
  private final RoleApplicationService roleApplicationService;

  @POST
  @PermissionsAllowed(Role.ADD)
  public Result<String, RoleVO> createRole(@Valid RoleCreationForm form) {
    return Results.ok(roleApplicationService.createRole(form));
  }

  @GET
  @PermissionsAllowed(Role.VIEW)
  public Result<String, ApiPageResult<RoleVO>> queryRolePage(@BeanParam @Valid RoleQuery query) {
    return Results.ok(roleApplicationService.queryRolePage(query));
  }

  @GET
  @Path("/{id}")
  @PermissionsAllowed(Role.VIEW)
  public Result<String, Optional<RoleVO>> getRoleById(@PathParam("id") Long id) {
    return Results.ok(roleApplicationService.getRoleById(id));
  }

  @PUT
  @Path("/{id}")
  @PermissionsAllowed(Role.EDIT)
  public Result<String, RoleVO> updateRole(@PathParam("id") Long id, @Valid UpdateRoleForm form) {
    return Results.ok(roleApplicationService.updateRole(id, form));
  }

  @DELETE
  @Path("/{id}")
  @PermissionsAllowed(Role.DELETE)
  public Result<String, Void> deleteRole(@PathParam("id") Long id) {
    roleApplicationService.deleteRole(id);
    return Results.ok();
  }

  @GET
  @Path("/name/{name}")
  @PermissionsAllowed(Role.VIEW)
  public Result<String, Optional<RoleVO>> getRoleByName(@PathParam("name") String name) {
    return Results.ok(roleApplicationService.getRoleByName(name));
  }

  @POST
  @Path("/assign/permission-group")
  @PermissionsAllowed(Role.EDIT)
  public Result<String, Void> assignPermissionGroups(@Valid RoleRefPermissionGroupForm form) {
    roleApplicationService.assignPermissionGroups(form);
    return Results.ok();
  }

  @GET
  @Path("/list")
  @PermissionsAllowed(Role.VIEW)
  public Result<String, List<RoleVO>> getAllRoles() {
    return Results.ok(roleApplicationService.getAllRoles());
  }

  @GET
  @Path("/count/code/{code}")
  @PermissionsAllowed(Role.VIEW)
  public Result<String, Long> countCode(@PathParam("code") String code) {
    return Results.ok(roleApplicationService.countCode(code));
  }

  @GET
  @Path("/count/roleTotal")
  @PermissionsAllowed(Role.VIEW)
  public Result<String, Long> countRoleTotal() {
    return Results.ok(roleApplicationService.countRole());
  }
}
