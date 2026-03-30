package com.github.DaiYuANg.modules.accesscontrol;

import com.github.DaiYuANg.common.model.ApiPageResult;
import com.github.DaiYuANg.common.model.Results;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVO;
import com.github.DaiYuANg.modules.accesscontrol.application.permission.PermissionApplicationService;
import com.github.DaiYuANg.modules.accesscontrol.application.permissiongroup.PermissionGroupApplicationService;
import com.github.DaiYuANg.modules.accesscontrol.PermissionGroupBindingForm;
import com.github.DaiYuANg.modules.accesscontrol.query.PermissionPageQueryParams;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.Permission;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.toolkit4j.data.model.envelope.Result;

@Path("/api/v1/permission")
@Produces("application/json")
@Consumes("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionResource {
  private final PermissionApplicationService permissionApplicationService;
  private final PermissionGroupApplicationService permissionGroupApplicationService;

  @GET
  @Path("/{id}")
  @PermissionsAllowed(Permission.VIEW)
  public Result<String, Optional<PermissionVO>> getById(@PathParam("id") Long id) {
    return Results.ok(permissionApplicationService.getPermissionById(id));
  }

  @GET
  @Path("/name/{name}")
  @PermissionsAllowed(Permission.VIEW)
  public Result<String, Optional<PermissionVO>> getByName(@PathParam("name") String name) {
    return Results.ok(permissionApplicationService.getPermissionByName(name));
  }

  @GET
  @PermissionsAllowed(Permission.VIEW)
  public Result<String, ApiPageResult<PermissionVO>> query(
      @BeanParam @Valid PermissionPageQueryParams query) {
    return Results.ok(permissionApplicationService.queryPermissionPage(query.toQuery()));
  }

  @GET
  @Path("/list")
  @PermissionsAllowed(Permission.VIEW)
  public Result<String, List<PermissionVO>> list() {
    return Results.ok(permissionApplicationService.getAllPermissions());
  }

  @PATCH
  @Path("/{id}")
  @PermissionsAllowed(Permission.EDIT)
  public Result<String, Void> bindGroup(@PathParam("id") Long id, PermissionGroupBindingForm form) {
    permissionGroupApplicationService.bindPermissionsToGroup(
        form == null ? null : form.groupId(), List.of(id));
    return Results.ok();
  }

  @PATCH
  @Path("/bulk")
  @PermissionsAllowed(Permission.EDIT)
  public Result<String, Void> bindGroupBulk(
      @QueryParam("id") String ids, PermissionGroupBindingForm form) {
    var permissionIds = parseIds(ids);
    permissionGroupApplicationService.bindPermissionsToGroup(
        form == null ? null : form.groupId(), permissionIds);
    return Results.ok();
  }

  private List<Long> parseIds(String rawIds) {
    if (rawIds == null || rawIds.isBlank()) {
      return List.of();
    }
    return java.util.Arrays.stream(rawIds.split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .map(Long::parseLong)
        .toList();
  }
}
