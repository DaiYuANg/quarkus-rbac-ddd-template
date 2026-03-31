package com.github.DaiYuANg.modules.accesscontrol.application.user.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdateUserForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.UserVOBuilder;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.UserVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.support.AccessControlAuditSupport;
import com.github.DaiYuANg.modules.accesscontrol.application.support.UserLifecycleSupport;
import com.github.DaiYuANg.modules.accesscontrol.application.user.UserChecker;
import com.github.DaiYuANg.modules.accesscontrol.application.user.UserApplicationService;
import com.github.DaiYuANg.security.auth.PasswordHasher;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UserApplicationServiceSessionInvalidationTest {

  @Test
  void updatePasswordRevokesAllRefreshTokensForUser() {
    var fixtures = fixtures();
    var user = new SysUser();
    user.id = 10L;
    user.username = "alice";
    user.password = "old";

    when(fixtures.userRepository.findByIdOptional(10L)).thenReturn(Optional.of(user));
    when(fixtures.passwordHasher.hash("new-pass")).thenReturn("hashed");

    fixtures.service.updateUserPassword(10L, "new-pass");

    verify(fixtures.userLifecycleSupport).onPasswordChanged(user);
  }

  @Test
  void deleteUserRevokesAllRefreshTokensForUser() {
    var fixtures = fixtures();
    var user = new SysUser();
    user.id = 11L;
    user.username = "bob";

    when(fixtures.userRepository.findByIdOptional(11L)).thenReturn(Optional.of(user));

    fixtures.service.deleteUser(11L);

    verify(fixtures.userLifecycleSupport).onUserDeleted(user);
    verify(fixtures.userRepository).deleteById(11L);
  }

  @Test
  void disablingUserRevokesAllRefreshTokensForUser() {
    var fixtures = fixtures();
    var user = new SysUser();
    user.id = 12L;
    user.username = "charlie";
    user.userStatus = UserStatus.ENABLED;

    when(fixtures.userRepository.findByIdOptional(12L)).thenReturn(Optional.of(user));

    fixtures.service.updateUserStatus(12L, 0);

    verify(fixtures.userLifecycleSupport).onStatusUpdated(user);
  }

  @Test
  void enablingUserDoesNotRevokeRefreshTokens() {
    var fixtures = fixtures();
    var user = new SysUser();
    user.id = 13L;
    user.username = "diana";
    user.userStatus = UserStatus.DISABLED;

    when(fixtures.userRepository.findByIdOptional(13L)).thenReturn(Optional.of(user));

    fixtures.service.updateUserStatus(13L, 1);

    verify(fixtures.userLifecycleSupport).onStatusUpdated(user);
  }

  @Test
  void renamingUserRevokesLegacyAndUserIdBoundSessions() {
    var fixtures = fixtures();
    var user = new SysUser();
    user.id = 14L;
    user.username = "erin";
    user.userStatus = UserStatus.ENABLED;

    when(fixtures.userRepository.findByIdOptional(14L)).thenReturn(Optional.of(user));
    when(fixtures.userRepository.findByIdWithRbacGraph(14L)).thenReturn(Optional.of(user));

    fixtures.service.updateUser(
        14L, new UpdateUserForm("erin-new", null, null, null, UserStatus.ENABLED, null));

    verify(fixtures.userLifecycleSupport).onUserUpdated(user, "erin", true);
  }

  @Test
  void deletingMissingUserThrowsNotFound() {
    var fixtures = fixtures();

    var ex = assertThrows(BizException.class, () -> fixtures.service.deleteUser(99L));

    assertEquals(ResultCode.DATA_NOT_FOUND, ex.getResultCode());
    verify(fixtures.userLifecycleSupport, never()).onUserDeleted(org.mockito.ArgumentMatchers.any());
    verify(fixtures.userRepository, never()).deleteById(99L);
  }

  private Fixtures fixtures() {
    var userRepository = mock(UserRepository.class);
    var passwordHasher = mock(PasswordHasher.class);
    var auditSupport = mock(AccessControlAuditSupport.class);
    var userLifecycleSupport = mock(UserLifecycleSupport.class);
    var userVOMapper = mock(UserVOMapper.class);
    var userChecker = mock(UserChecker.class);

    when(userRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());
    when(userVOMapper.toVO(org.mockito.ArgumentMatchers.any(SysUser.class)))
        .thenAnswer(
            invocation -> {
              var user = invocation.getArgument(0, SysUser.class);
              return UserVOBuilder.builder()
                  .id(user.id)
                  .username(user.username)
                  .identifier(user.identifier)
                  .mobilePhone(user.mobilePhone)
                  .nickname(user.nickname)
                  .email(user.email)
                  .latestSignIn(user.latestSignIn)
                  .createAt(user.createAt)
                  .updateAt(user.updateAt)
                  .userStatus(user.userStatus)
                  .roles(java.util.Set.of())
                  .build();
            });

    var service =
        new UserApplicationService(
            userRepository,
            passwordHasher,
            auditSupport,
            userLifecycleSupport,
            userVOMapper,
            userChecker);
    return new Fixtures(service, userRepository, passwordHasher, userLifecycleSupport);
  }

  private record Fixtures(
      UserApplicationService service,
      UserRepository userRepository,
      PasswordHasher passwordHasher,
      UserLifecycleSupport userLifecycleSupport) {}
}
