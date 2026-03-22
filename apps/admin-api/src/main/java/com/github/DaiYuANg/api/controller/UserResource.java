package com.github.DaiYuANg.api.controller;

import com.github.DaiYuANg.api.dto.request.ChangePasswordForm;
import com.github.DaiYuANg.api.dto.request.UpdateUserForm;
import com.github.DaiYuANg.api.dto.request.UserCreationForm;
import com.github.DaiYuANg.api.dto.request.UserRefRoleForm;
import com.github.DaiYuANg.api.dto.response.UserVO;
import com.github.DaiYuANg.application.user.UserApplicationService;
import com.github.DaiYuANg.common.model.PageResult;
import com.github.DaiYuANg.common.model.Result;
import com.github.DaiYuANg.identity.parameter.UserQuery;
import io.quarkus.security.PermissionsAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Path("/api/v1/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserResource {
    private final UserApplicationService userApplicationService;

    @GET @PermissionsAllowed("system.user:view")
    public Result<PageResult<UserVO>> queryUserPage(@BeanParam @Valid UserQuery query) { return Result.ok(userApplicationService.queryUserPage(query)); }
    @POST @PermissionsAllowed("system.user:add")
    public Result<UserVO> createUser(@Valid UserCreationForm form) { return Result.ok(userApplicationService.createUser(form)); }
    @PUT @Path("/{id}/password") @PermissionsAllowed("system.user:edit")
    public Result<Void> updateUserPassword(@PathParam("id") Long id, @Valid ChangePasswordForm form) { userApplicationService.updateUserPassword(id, form.newPassword()); return Result.ok(); }
    @GET @Path("/{id}") @PermissionsAllowed("system.user:view")
    public Result<Optional<UserVO>> getUserById(@PathParam("id") Long id) { return Result.ok(userApplicationService.getUserById(id)); }
    @GET @Path("/list") @PermissionsAllowed("system.user:view")
    public Result<List<UserVO>> getAllUsers() { return Result.ok(userApplicationService.getAllUsers()); }
    @PUT @Path("/{id}") @PermissionsAllowed("system.user:edit")
    public Result<UserVO> updateUser(@PathParam("id") Long id, @Valid UpdateUserForm form) { return Result.ok(userApplicationService.updateUser(id, form)); }
    @DELETE @Path("/{id}") @PermissionsAllowed("system.user:delete")
    public Result<Void> deleteUser(@PathParam("id") Long id) { userApplicationService.deleteUser(id); return Result.ok(); }
    @GET @Path("/username/{username}") @PermissionsAllowed("system.user:view")
    public Result<Optional<UserVO>> getUserByUsername(@PathParam("username") String username) { return Result.ok(userApplicationService.getUserByUsername(username)); }
    @POST @Path("/assign/role") @PermissionsAllowed("system.user:edit")
    public Result<Void> assignRole(@Valid UserRefRoleForm form) { userApplicationService.assignRole(form); return Result.ok(); }
    @GET @Path("/count/email/{email}") public Result<Long> countEmail(@PathParam("email") String email) { return Result.ok(userApplicationService.countEmail(email)); }
    @GET @Path("/count/username/{username}") public Result<Long> countUsername(@PathParam("username") String username) { return Result.ok(userApplicationService.countUsername(username)); }
    @GET @Path("/count/mobilePhone/{mobilePhone}") public Result<Long> countMobilePhone(@PathParam("mobilePhone") String mobilePhone) { return Result.ok(userApplicationService.countMobilePhone(mobilePhone)); }
    @GET @Path("/count/identifier/{identifier}") public Result<Long> countIdentifier(@PathParam("identifier") String identifier) { return Result.ok(userApplicationService.countIdentifier(identifier)); }
    @PUT @Path("/{id}/status") @PermissionsAllowed("system.user:edit") public Result<Void> updateUserStatus(@PathParam("id") Long id, @QueryParam("status") Integer status) { userApplicationService.updateUserStatus(id, status); return Result.ok(); }
    @GET @Path("/count/userTotal") public Result<Long> countUserTotal() { return Result.ok(userApplicationService.countUserTotal()); }
    @GET @Path("/count/userLoginTotal") public Result<Long> countUserLoginTotal() { return Result.ok(userApplicationService.countUserLoginTotal()); }
}
