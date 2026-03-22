package com.github.DaiYuANg.api.controller;

import com.github.DaiYuANg.accesscontrol.parameter.RoleQuery;
import com.github.DaiYuANg.api.controller.support.ExportResponseHelper;
import com.github.DaiYuANg.api.dto.request.RoleCreationForm;
import com.github.DaiYuANg.api.dto.request.RoleRefPermissionGroupForm;
import com.github.DaiYuANg.api.dto.request.UpdateRoleForm;
import com.github.DaiYuANg.api.dto.response.RoleVO;
import com.github.DaiYuANg.application.converter.ExportMapper;
import com.github.DaiYuANg.application.role.RoleApplicationService;
import com.github.DaiYuANg.common.model.PageResult;
import com.github.DaiYuANg.common.model.Result;
import com.github.DaiYuANg.export.model.ExportRequest;
import com.github.DaiYuANg.export.spi.ExcelExporter;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Path("/api/v1/role")
@Produces("application/json")
@Consumes("application/json")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RoleResource {
    private final RoleApplicationService roleApplicationService;
    private final ExcelExporter excelExporter;
    private final ExportMapper exportMapper;

    @POST @PermissionsAllowed("system.role:add") public Result<RoleVO> createRole(@Valid RoleCreationForm form) { return Result.ok(roleApplicationService.createRole(form)); }
    @GET @PermissionsAllowed("system.role:view") public Result<PageResult<RoleVO>> queryRolePage(@BeanParam @Valid RoleQuery query) { return Result.ok(roleApplicationService.queryRolePage(query)); }
    @GET @Path("/{id}") @PermissionsAllowed("system.role:view") public Result<Optional<RoleVO>> getRoleById(@PathParam("id") Long id) { return Result.ok(roleApplicationService.getRoleById(id)); }
    @PUT @Path("/{id}") @PermissionsAllowed("system.role:edit") public Result<RoleVO> updateRole(@PathParam("id") Long id, @Valid UpdateRoleForm form) { return Result.ok(roleApplicationService.updateRole(id, form)); }
    @DELETE @Path("/{id}") @PermissionsAllowed("system.role:delete") public Result<Void> deleteRole(@PathParam("id") Long id) { roleApplicationService.deleteRole(id); return Result.ok(); }
    @GET @Path("/name/{name}") @PermissionsAllowed("system.role:view") public Result<Optional<RoleVO>> getRoleByName(@PathParam("name") String name) { return Result.ok(roleApplicationService.getRoleByName(name)); }
    @POST @Path("/assign/permission-group") @PermissionsAllowed("system.role:edit") public Result<Void> assignPermissionGroups(@Valid RoleRefPermissionGroupForm form) { roleApplicationService.assignPermissionGroups(form); return Result.ok(); }
    @GET @Path("/list") @PermissionsAllowed("system.role:view") public Result<List<RoleVO>> getAllRoles() { return Result.ok(roleApplicationService.getAllRoles()); }
    @GET @Path("/count/code/{code}") public Result<Long> countCode(@PathParam("code") String code) { return Result.ok(roleApplicationService.countCode(code)); }
    @GET @Path("/count/roleTotal") public Result<Long> countRoleTotal() { return Result.ok(roleApplicationService.countRole()); }
    @GET @Path("/export") @PermissionsAllowed("system.role:view") public Response export() {
        var rows = roleApplicationService.getAllRoles().stream().map(exportMapper::toRoleExportRow).toList();
        return ExportResponseHelper.attachment(excelExporter.export(new ExportRequest("roles", rows)));
    }
}
