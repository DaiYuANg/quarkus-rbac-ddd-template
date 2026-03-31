package com.github.DaiYuANg.modules.accesscontrol.application.user;

import com.github.DaiYuANg.accesscontrol.repository.RoleRepository;
import com.github.DaiYuANg.cache.PermissionSnapshotStore;
import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.common.model.ApiPageResult;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.query.UserPageQuery;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.UserVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdateUserForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UserCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UserRefRoleForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.UserVO;
import com.github.DaiYuANg.modules.accesscontrol.application.support.AccessControlAuditSupport;
import com.github.DaiYuANg.security.auth.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * User management application service.
 *
 * <p>Performance notes:
 *
 * <ul>
 *   <li>When returning nested RBAC views (roles -> permissionGroups -> permissions), reload with
 *       RBAC fetch graph helpers to avoid N+1 lazy loads.
 *   <li>Role assignment uses bulk repository loading (no per-id DB calls).
 * </ul>
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserApplicationService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordHasher passwordHasher;
  private final AccessControlAuditSupport auditSupport;
  private final PermissionSnapshotStore permissionSnapshotStore;
  private final RefreshTokenStore refreshTokenStore;
  private final UserVOMapper userVOMapper;
  private final UserChecker userChecker;

  public ApiPageResult<UserVO> queryUserPage(@NonNull UserPageQuery query) {
    return ApiPageResult.map(userRepository.page(query), userVOMapper::toProjectionVO);
  }

  @Transactional
  public UserVO createUser(@NonNull UserCreationForm form) {
    userChecker.ensureCreatable(form);
    val user = userVOMapper.toEntity(form, passwordHasher);
    userRepository.persist(user);
    auditSupport.bumpGlobalVersion();
    auditSupport.record("user", "create", form.username(), true, "create user");
    return userVOMapper.toVO(user);
  }

  @Transactional
  public void updateUserPassword(@NonNull Long id, @NonNull String newPassword) {
    val user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    user.password = passwordHasher.hash(newPassword);
    permissionSnapshotStore.delete(user.id);
    refreshTokenStore.deleteByUserId(user.id);
    refreshTokenStore.deleteByUsername(user.username);
    auditSupport.bumpGlobalVersion();
    auditSupport.record("user", "change-password", String.valueOf(id), true, "change password");
  }

  public Optional<UserVO> getUserById(@NonNull Long id) {
    return userRepository.findByIdWithRbacGraph(id).map(userVOMapper::toVO);
  }

  public List<UserVO> getAllUsers() {
    return userRepository.listAllWithRbacGraph().stream().map(userVOMapper::toVO).toList();
  }

  @Transactional
  public UserVO updateUser(@NonNull Long id, @NonNull UpdateUserForm form) {
    val user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    val originalUsername = user.username;
    userChecker.ensureUpdatable(user, form);
    val usernameChanged = form.username() != null && !form.username().equals(user.username);
    userVOMapper.updateEntity(form, user);
    if (form.roleIds() != null) {
      user.roles.clear();
      user.roles.addAll(roleRepository.findAllByIds(form.roleIds()));
    }
    permissionSnapshotStore.delete(user.id);
    if (usernameChanged) {
      refreshTokenStore.deleteByUserId(user.id);
      refreshTokenStore.deleteByUsername(originalUsername);
    }
    if (user.userStatus == UserStatus.DISABLED) {
      refreshTokenStore.deleteByUserId(user.id);
      refreshTokenStore.deleteByUsername(user.username);
    }
    auditSupport.bumpGlobalVersion();
    auditSupport.record("user", "update", user.username, true, "update user");
    // Avoid N+1 lazy loads when mapping nested RBAC graph in UserVO.
    return userRepository
        .findByIdWithRbacGraph(user.id)
        .map(userVOMapper::toVO)
        .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
  }

  @Transactional
  public void deleteUser(@NonNull Long id) {
    val user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    refreshTokenStore.deleteByUserId(user.id);
    refreshTokenStore.deleteByUsername(user.username);
    permissionSnapshotStore.delete(id);
    userRepository.deleteById(id);
    auditSupport.bumpGlobalVersion();
    auditSupport.record("user", "delete", String.valueOf(id), true, "delete user");
  }

  public Optional<UserVO> getUserByUsername(@NonNull String username) {
    return userRepository.findByUsernameWithRbacGraph(username).map(userVOMapper::toVO);
  }

  @Transactional
  public void assignRole(@NonNull UserRefRoleForm form) {
    val user =
        userRepository
            .findByIdOptional(form.userId())
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    user.roles.clear();
    if (form.roleIds() != null) {
      user.roles.addAll(roleRepository.findAllByIds(form.roleIds()));
    }
    permissionSnapshotStore.delete(user.id);
    auditSupport.bumpGlobalVersion();
    auditSupport.record(
        "user", "assign-role", String.valueOf(form.userId()), true, "assign user roles");
  }

  @Transactional
  public void updateUserStatus(@NonNull Long id, Integer status) {
    val user =
        userRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    user.userStatus = (status != null && status == 1) ? UserStatus.ENABLED : UserStatus.DISABLED;
    permissionSnapshotStore.delete(user.id);
    if (user.userStatus == UserStatus.DISABLED) {
      refreshTokenStore.deleteByUserId(user.id);
      refreshTokenStore.deleteByUsername(user.username);
    }
    auditSupport.bumpGlobalVersion();
    auditSupport.record("user", "status", String.valueOf(id), true, "update user status");
  }

  public long countEmail(String email) {
    return userRepository.countByEmail(email);
  }

  public long countUsername(String username) {
    return userRepository.countByUsername(username);
  }

  public long countMobilePhone(String mobilePhone) {
    return userRepository.countByMobilePhone(mobilePhone);
  }

  public long countIdentifier(String identifier) {
    return userRepository.countByIdentifier(identifier);
  }

  public long countUserTotal() {
    return userRepository.count();
  }

  public long countUserLoginTotal() {
    return userRepository.countUserLoginTotal();
  }
}
