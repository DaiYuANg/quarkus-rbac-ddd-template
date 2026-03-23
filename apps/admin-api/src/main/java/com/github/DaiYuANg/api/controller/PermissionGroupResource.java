package com.github.DaiYuANg.api.controller;

import com.github.DaiYuANg.accesscontrol.parameter.PermissionGroupQuery;
import com.github.DaiYuANg.api.dto.request.PermissionGroupCreationForm;
import com.github.DaiYuANg.api.dto.request.PermissionGroupRefPermissionForm;
import com.github.DaiYuANg.api.dto.request.UpdatePermissionGroupForm;
import com.github.DaiYuANg.api.dto.response.PermissionGroupVO;
import com.github.DaiYuANg.application.permissiongroup.PermissionGroupApplicationService;
import com.github.DaiYuANg.common.model.PageResult;
import com.github.DaiYuANg.common.model.Result;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Path("/api/v1/permission-group")
@Produces("application/json")
@Consumes("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionGroupResource {
    private final PermissionGroupApplicationService permissionGroupApplicationService;

    @POST @PermissionsAllowed("permission-group:add") public Result<PermissionGroupVO> create(@Valid PermissionGroupCreationForm form) { return Result.ok(permissionGroupApplicationService.createPermissionGroup(form)); }
    @GET @Path("/{id}") @PermissionsAllowed("permission-group:view") public Result<Optional<PermissionGroupVO>> getById(@PathParam("id") Long id) { return Result.ok(permissionGroupApplicationService.getPermissionGroupById(id)); }
    @PUT @Path("/{id}") @PermissionsAllowed("permission-group:edit") public Result<PermissionGroupVO> update(@PathParam("id") Long id, @Valid UpdatePermissionGroupForm form) { return Result.ok(permissionGroupApplicationService.updatePermissionGroup(id, form)); }
    @DELETE @Path("/{id}") @PermissionsAllowed("permission-group:delete") public Result<Void> delete(@PathParam("id") Long id) { permissionGroupApplicationService.deletePermissionGroup(id); return Result.ok(); }
    @GET @PermissionsAllowed("permission-group:view") public Result<PageResult<PermissionGroupVO>> query(@BeanParam @Valid PermissionGroupQuery query) { return Result.ok(permissionGroupApplicationService.queryPermissionGroupPage(query)); }
    @GET @Path("/name/{name}") @PermissionsAllowed("permission-group:view") public Result<Optional<PermissionGroupVO>> getByName(@PathParam("name") String name) { return Result.ok(permissionGroupApplicationService.getPermissionGroupByName(name)); }
    @POST @Path("/assign/permission") @PermissionsAllowed("permission-group:edit") public Result<Void> assignPermissions(@Valid PermissionGroupRefPermissionForm form) { permissionGroupApplicationService.assignPermissions(form); return Result.ok(); }
    @GET @Path("/list") @PermissionsAllowed("permission-group:view") public Result<List<PermissionGroupVO>> list() { return Result.ok(permissionGroupApplicationService.getAllPermissionGroups()); }
    @GET @Path("/count/name/{name}") public Result<Long> countName(@PathParam("name") String name) { return Result.ok(permissionGroupApplicationService.countName(name)); }
    @GET @Path("/count") public Result<Long> count() { return Result.ok(permissionGroupApplicationService.count()); }
}
