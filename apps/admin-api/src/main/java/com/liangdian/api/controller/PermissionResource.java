package com.liangdian.api.controller;

import com.liangdian.accesscontrol.parameter.PermissionQuery;
import com.liangdian.api.controller.support.ExportResponseHelper;
import com.liangdian.api.dto.response.PermissionVO;
import com.liangdian.application.converter.ExportMapper;
import com.liangdian.application.permission.PermissionApplicationService;
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

@Path("/api/v1/permission")
@Produces("application/json")
@Consumes("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionResource {
    private final PermissionApplicationService permissionApplicationService;
    private final ExcelExporter excelExporter;
    private final ExportMapper exportMapper;

    @GET @Path("/{id}") @PermissionsAllowed("system.permission:view") public Result<Optional<PermissionVO>> getById(@PathParam("id") Long id) { return Result.ok(permissionApplicationService.getPermissionById(id)); }
    @GET @Path("/name/{name}") @PermissionsAllowed("system.permission:view") public Result<Optional<PermissionVO>> getByName(@PathParam("name") String name) { return Result.ok(permissionApplicationService.getPermissionByName(name)); }
    @GET @PermissionsAllowed("system.permission:view") public Result<PageResult<PermissionVO>> query(@BeanParam @Valid PermissionQuery query) { return Result.ok(permissionApplicationService.queryPermissionPage(query)); }
    @GET @Path("/list") @PermissionsAllowed("system.permission:view") public Result<List<PermissionVO>> list() { return Result.ok(permissionApplicationService.getAllPermissions()); }
    @GET @Path("/export") @PermissionsAllowed("system.permission:view") public Response export() {
        var rows = permissionApplicationService.getAllPermissions().stream().map(exportMapper::toPermissionExportRow).toList();
        return ExportResponseHelper.attachment(excelExporter.export(new ExportRequest("permissions", rows)));
    }
}
