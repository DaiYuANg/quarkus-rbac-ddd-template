package com.github.DaiYuANg.api.controller;

import com.github.DaiYuANg.accesscontrol.parameter.PermissionQuery;
import com.github.DaiYuANg.api.dto.response.PermissionVO;
import com.github.DaiYuANg.application.permission.PermissionApplicationService;
import com.github.DaiYuANg.common.model.PageResult;
import com.github.DaiYuANg.common.model.Result;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Path("/api/v1/permission")
@Produces("application/json")
@Consumes("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionResource {
    private final PermissionApplicationService permissionApplicationService;

    @GET @Path("/{id}") @PermissionsAllowed("permission:view") public Result<Optional<PermissionVO>> getById(@PathParam("id") Long id) { return Result.ok(permissionApplicationService.getPermissionById(id)); }
    @GET @Path("/name/{name}") @PermissionsAllowed("permission:view") public Result<Optional<PermissionVO>> getByName(@PathParam("name") String name) { return Result.ok(permissionApplicationService.getPermissionByName(name)); }
    @GET @PermissionsAllowed("permission:view") public Result<PageResult<PermissionVO>> query(@BeanParam @Valid PermissionQuery query) { return Result.ok(permissionApplicationService.queryPermissionPage(query)); }
    @GET @Path("/list") @PermissionsAllowed("permission:view") public Result<List<PermissionVO>> list() { return Result.ok(permissionApplicationService.getAllPermissions()); }
}
