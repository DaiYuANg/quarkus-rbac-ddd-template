package com.github.DaiYuANg.modules.accesscontrol;

import com.github.DaiYuANg.accesscontrol.parameter.PermissionQuery;
import com.github.DaiYuANg.common.model.PageResult;
import com.github.DaiYuANg.common.model.Result;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVO;
import com.github.DaiYuANg.modules.accesscontrol.application.permission.PermissionApplicationService;
import com.github.DaiYuANg.modules.accesscontrol.application.permissiongroup.PermissionGroupApplicationService;
import com.github.DaiYuANg.modules.accesscontrol.dto.PermissionGroupBindingForm;
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
    private final PermissionGroupApplicationService permissionGroupApplicationService;

    @GET @Path("/{id}") @PermissionsAllowed("permission:view") public Result<Optional<PermissionVO>> getById(@PathParam("id") Long id) { return Result.ok(permissionApplicationService.getPermissionById(id)); }
    @GET @Path("/name/{name}") @PermissionsAllowed("permission:view") public Result<Optional<PermissionVO>> getByName(@PathParam("name") String name) { return Result.ok(permissionApplicationService.getPermissionByName(name)); }
    @GET @PermissionsAllowed("permission:view") public Result<PageResult<PermissionVO>> query(@BeanParam @Valid PermissionQuery query) { return Result.ok(permissionApplicationService.queryPermissionPage(query)); }
    @GET @Path("/list") @PermissionsAllowed("permission:view") public Result<List<PermissionVO>> list() { return Result.ok(permissionApplicationService.getAllPermissions()); }

    @PATCH @Path("/{id}") @PermissionsAllowed("permission:edit")
    public Result<Void> bindGroup(@PathParam("id") Long id, PermissionGroupBindingForm form) {
        permissionGroupApplicationService.bindPermissionsToGroup(form == null ? null : form.groupId(), List.of(id));
        return Result.ok();
    }

    @PATCH @Path("/bulk") @PermissionsAllowed("permission:edit")
    public Result<Void> bindGroupBulk(@QueryParam("id") String ids, PermissionGroupBindingForm form) {
        var permissionIds = parseIds(ids);
        permissionGroupApplicationService.bindPermissionsToGroup(form == null ? null : form.groupId(), permissionIds);
        return Result.ok();
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
