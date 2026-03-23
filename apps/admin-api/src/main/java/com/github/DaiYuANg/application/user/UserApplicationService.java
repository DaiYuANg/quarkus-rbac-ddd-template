package com.github.DaiYuANg.application.user;

import com.github.DaiYuANg.accesscontrol.repository.RoleRepository;
import com.github.DaiYuANg.api.dto.request.UpdateUserForm;
import com.github.DaiYuANg.api.dto.request.UserCreationForm;
import com.github.DaiYuANg.api.dto.request.UserRefRoleForm;
import com.github.DaiYuANg.api.dto.response.UserVO;
import com.github.DaiYuANg.application.audit.AuthorityVersionService;
import com.github.DaiYuANg.application.audit.OperationLogService;
import com.github.DaiYuANg.application.converter.ViewMapper;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.common.model.PageResult;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.parameter.UserQuery;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.security.AuthorizationService;
import com.github.DaiYuANg.security.CurrentUserAccess;
import com.github.DaiYuANg.security.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserApplicationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHasher passwordHasher;
    private final ViewMapper mapper;
    private final AuthorityVersionService authorityVersionService;
    private final OperationLogService operationLogService;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccess currentUserAccess;

    public PageResult<UserVO> queryUserPage(UserQuery query) {
        authorizationService.check("user", "view");
        var slice = userRepository.page(query);
        return PageResult.of(slice.total(), query.getPageNum(), query.getPageSize(),
            slice.content().stream().map(mapper::toUserVO).toList());
    }

    @Transactional
    public UserVO createUser(UserCreationForm form) {
        authorizationService.check("user", "add");
        if (userRepository.countByUsername(form.username()) > 0) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "username already exists");
        if (form.email() != null && !form.email().isBlank() && userRepository.countByEmail(form.email()) > 0) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "email already exists");
        if (form.mobilePhone() != null && !form.mobilePhone().isBlank() && userRepository.countByMobilePhone(form.mobilePhone()) > 0) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "mobilePhone already exists");
        var user = new SysUser();
        user.username = form.username();
        user.password = passwordHasher.hash(form.password());
        user.mobilePhone = form.mobilePhone();
        user.nickname = form.nickname();
        user.email = form.email();
        user.userStatus = form.userStatus() == null ? UserStatus.ENABLED : form.userStatus();
        userRepository.persist(user);
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("user", "create", form.username(), true, "create user");
        return mapper.toUserVO(user);
    }

    @Transactional
    public void updateUserPassword(Long id, String newPassword) {
        var user = userRepository.findByIdOptional(id).orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
        var currentUsername = currentUserAccess.currentUser().map(com.github.DaiYuANg.security.CurrentAuthenticatedUser::username).orElse(null);
        if (currentUsername != null && currentUsername.equals(user.username)) {
            authorizationService.checkAny("auth:change-password", "user:reset-password", "user:edit");
        } else {
            authorizationService.checkAny("user:reset-password", "user:edit");
        }
        user.password = passwordHasher.hash(newPassword);
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("user", "change-password", String.valueOf(id), true, "change password");
    }

    public Optional<UserVO> getUserById(Long id) { authorizationService.check("user", "view"); return userRepository.findByIdOptional(id).map(mapper::toUserVO); }
    public List<UserVO> getAllUsers() { authorizationService.check("user", "view"); return userRepository.listAll().stream().map(mapper::toUserVO).toList(); }

    @Transactional
    public UserVO updateUser(Long id, UpdateUserForm form) {
        authorizationService.check("user", "edit");
        var user = userRepository.findByIdOptional(id).orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
        if (form.username() != null && !form.username().equals(user.username) && userRepository.countByUsername(form.username()) > 0) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "username already exists");
        if (form.email() != null && !form.email().equals(user.email) && userRepository.countByEmail(form.email()) > 0) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "email already exists");
        if (form.mobilePhone() != null && !form.mobilePhone().equals(user.mobilePhone) && userRepository.countByMobilePhone(form.mobilePhone()) > 0) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "mobilePhone already exists");
        if (form.username() != null) user.username = form.username();
        if (form.mobilePhone() != null) user.mobilePhone = form.mobilePhone();
        if (form.nickname() != null) user.nickname = form.nickname();
        if (form.email() != null) user.email = form.email();
        if (form.status() != null) user.userStatus = form.status();
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("user", "update", user.username, true, "update user");
        return mapper.toUserVO(user);
    }

    @Transactional
    public void deleteUser(Long id) { authorizationService.check("user", "delete"); userRepository.deleteById(id); authorityVersionService.bumpGlobalVersion(); operationLogService.record("user", "delete", String.valueOf(id), true, "delete user"); }
    public Optional<UserVO> getUserByUsername(String username) { authorizationService.check("user", "view"); return userRepository.findByUsername(username).map(mapper::toUserVO); }

    @Transactional
    public void assignRole(UserRefRoleForm form) {
        authorizationService.checkAny("user:edit", "user:assign-role");
        var user = userRepository.findByIdOptional(form.userId()).orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
        user.roles.clear();
        if (form.roleIds() != null) {
            form.roleIds().forEach(rid -> roleRepository.findByIdOptional(rid).ifPresent(user.roles::add));
        }
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("user", "assign-role", String.valueOf(form.userId()), true, "assign user roles");
    }

    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        authorizationService.check("user", "edit");
        var user = userRepository.findByIdOptional(id).orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
        user.userStatus = (status != null && status == 1) ? UserStatus.ENABLED : UserStatus.DISABLED;
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("user", "status", String.valueOf(id), true, "update user status");
    }

    public long countEmail(String email) { return userRepository.countByEmail(email); }
    public long countUsername(String username) { return userRepository.countByUsername(username); }
    public long countMobilePhone(String mobilePhone) { return userRepository.countByMobilePhone(mobilePhone); }
    public long countIdentifier(String identifier) { return userRepository.countByIdentifier(identifier); }
    public long countUserTotal() { return userRepository.count(); }
    public long countUserLoginTotal() { return userRepository.count("latestSignIn is not null"); }
}
