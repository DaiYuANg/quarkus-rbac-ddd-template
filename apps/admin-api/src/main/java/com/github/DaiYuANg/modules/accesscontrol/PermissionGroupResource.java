package com.github.DaiYuANg.modules.accesscontrol;

import com.github.DaiYuANg.accesscontrol.parameter.PermissionGroupQuery;
import com.github.DaiYuANg.common.model.ApiPageResult;
import com.github.DaiYuANg.common.model.Results;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.PermissionGroupCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.PermissionGroupRefPermissionForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdatePermissionGroupForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionGroupVO;
import com.github.DaiYuANg.modules.accesscontrol.application.permissiongroup.PermissionGroupApplicationService;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.PermissionGroup;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.toolkit4j.data.model.envelope.Result;

@Path("/api/v1/permission-group")
@Produces("application/json")
@Consumes("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionGroupResource {
  private final PermissionGroupApplicationService permissionGroupApplicationService;

  @POST
  @PermissionsAllowed(PermissionGroup.ADD)
  public Result<String, PermissionGroupVO> create(@Valid PermissionGroupCreationForm form) {
    return Results.ok(permissionGroupApplicationService.createPermissionGroup(form));
  }

  @GET
  @Path("/{id}")
  @PermissionsAllowed(PermissionGroup.VIEW)
  public Result<String, Optional<PermissionGroupVO>> getById(@PathParam("id") Long id) {
    return Results.ok(permissionGroupApplicationService.getPermissionGroupById(id));
  }

  @PUT
  @Path("/{id}")
  @PermissionsAllowed(PermissionGroup.EDIT)
  public Result<String, PermissionGroupVO> update(
      @PathParam("id") Long id, @Valid UpdatePermissionGroupForm form) {
    return Results.ok(permissionGroupApplicationService.updatePermissionGroup(id, form));
  }

  @DELETE
  @Path("/{id}")
  @PermissionsAllowed(PermissionGroup.DELETE)
  public Result<String, Void> delete(@PathParam("id") Long id) {
    permissionGroupApplicationService.deletePermissionGroup(id);
    return Results.ok();
  }

  @GET
  @PermissionsAllowed(PermissionGroup.VIEW)
  public Result<String, ApiPageResult<PermissionGroupVO>> query(
      @BeanParam @Valid PermissionGroupQuery query) {
    return Results.ok(permissionGroupApplicationService.queryPermissionGroupPage(query));
  }

  @GET
  @Path("/name/{name}")
  @PermissionsAllowed(PermissionGroup.VIEW)
  public Result<String, Optional<PermissionGroupVO>> getByName(@PathParam("name") String name) {
    return Results.ok(permissionGroupApplicationService.getPermissionGroupByName(name));
  }

  @POST
  @Path("/assign/permission")
  @PermissionsAllowed(PermissionGroup.EDIT)
  public Result<String, Void> assignPermissions(@Valid PermissionGroupRefPermissionForm form) {
    permissionGroupApplicationService.assignPermissions(form);
    return Results.ok();
  }

  @GET
  @Path("/list")
  @PermissionsAllowed(PermissionGroup.VIEW)
  public Result<String, List<PermissionGroupVO>> list() {
    return Results.ok(permissionGroupApplicationService.getAllPermissionGroups());
  }

  @GET
  @Path("/count/name/{name}")
  public Result<String, Long> countName(@PathParam("name") String name) {
    return Results.ok(permissionGroupApplicationService.countName(name));
  }

  @GET
  @Path("/count")
  public Result<String, Long> count() {
    return Results.ok(permissionGroupApplicationService.count());
  }
}
