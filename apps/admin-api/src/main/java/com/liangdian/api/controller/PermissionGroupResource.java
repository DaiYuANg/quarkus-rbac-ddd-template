package com.liangdian.api.controller;

import com.liangdian.accesscontrol.parameter.PermissionGroupQuery;
import com.liangdian.api.controller.support.ExportResponseHelper;
import com.liangdian.api.dto.request.PermissionGroupCreationForm;
import com.liangdian.api.dto.request.PermissionGroupRefPermissionForm;
import com.liangdian.api.dto.request.UpdatePermissionGroupForm;
import com.liangdian.api.dto.response.PermissionGroupVO;
import com.liangdian.application.converter.ExportMapper;
import com.liangdian.application.permissiongroup.PermissionGroupApplicationService;
import com.liangdian.common.model.PageResult;
import com.liangdian.common.model.Result;
import com.liangdian.export.model.ExportRequest;
import com.liangdian.export.spi.ExcelExporter;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Path("/api/v1/permission-group")
@Produces("application/json")
@Consumes("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionGroupResource {
    private final PermissionGroupApplicationService permissionGroupApplicationService;
    private final ExcelExporter excelExporter;
    private final ExportMapper exportMapper;

    @POST @PermissionsAllowed("system.permission-group:add") public Result<PermissionGroupVO> create(@Valid PermissionGroupCreationForm form) { return Result.ok(permissionGroupApplicationService.createPermissionGroup(form)); }
    @GET @Path("/{id}") @PermissionsAllowed("system.permission-group:view") public Result<Optional<PermissionGroupVO>> getById(@PathParam("id") Long id) { return Result.ok(permissionGroupApplicationService.getPermissionGroupById(id)); }
    @PUT @Path("/{id}") @PermissionsAllowed("system.permission-group:edit") public Result<PermissionGroupVO> update(@PathParam("id") Long id, @Valid UpdatePermissionGroupForm form) { return Result.ok(permissionGroupApplicationService.updatePermissionGroup(id, form)); }
    @DELETE @Path("/{id}") @PermissionsAllowed("system.permission-group:delete") public Result<Void> delete(@PathParam("id") Long id) { permissionGroupApplicationService.deletePermissionGroup(id); return Result.ok(); }
    @GET @PermissionsAllowed("system.permission-group:view") public Result<PageResult<PermissionGroupVO>> query(@BeanParam @Valid PermissionGroupQuery query) { return Result.ok(permissionGroupApplicationService.queryPermissionGroupPage(query)); }
    @GET @Path("/name/{name}") @PermissionsAllowed("system.permission-group:view") public Result<Optional<PermissionGroupVO>> getByName(@PathParam("name") String name) { return Result.ok(permissionGroupApplicationService.getPermissionGroupByName(name)); }
    @POST @Path("/assign/permission") @PermissionsAllowed("system.permission-group:edit") public Result<Void> assignPermissions(@Valid PermissionGroupRefPermissionForm form) { permissionGroupApplicationService.assignPermissions(form); return Result.ok(); }
    @GET @Path("/list") @PermissionsAllowed("system.permission-group:view") public Result<List<PermissionGroupVO>> list() { return Result.ok(permissionGroupApplicationService.getAllPermissionGroups()); }
    @GET @Path("/count/name/{name}") public Result<Long> countName(@PathParam("name") String name) { return Result.ok(permissionGroupApplicationService.countName(name)); }
    @GET @Path("/count") public Result<Long> count() { return Result.ok(permissionGroupApplicationService.count()); }
    @GET @Path("/export") @PermissionsAllowed("system.permission-group:view") public Response export() {
        var rows = permissionGroupApplicationService.getAllPermissionGroups().stream().map(exportMapper::toPermissionGroupExportRow).toList();
        return ExportResponseHelper.attachment(excelExporter.export(new ExportRequest("permission-groups", rows)));
    }
}
