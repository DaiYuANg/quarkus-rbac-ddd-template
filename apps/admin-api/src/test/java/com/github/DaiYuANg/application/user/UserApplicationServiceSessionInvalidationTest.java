package com.github.DaiYuANg.application.user;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.accesscontrol.repository.RoleRepository;
import com.github.DaiYuANg.application.audit.AuthorityVersionService;
import com.github.DaiYuANg.application.audit.OperationLogService;
import com.github.DaiYuANg.application.converter.ViewMapper;
import com.github.DaiYuANg.cache.PermissionSnapshotStore;
import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.security.AuthorizationService;
import com.github.DaiYuANg.security.CurrentUserAccess;
import com.github.DaiYuANg.security.PasswordHasher;
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
    when(fixtures.currentUserAccess.currentUser()).thenReturn(Optional.empty());

    fixtures.service.updateUserPassword(10L, "new-pass");

    verify(fixtures.refreshTokenStore).deleteByUsername("alice");
  }

  @Test
  void deleteUserRevokesAllRefreshTokensForUser() {
    var fixtures = fixtures();
    var user = new SysUser();
    user.id = 11L;
    user.username = "bob";

    when(fixtures.userRepository.findByIdOptional(11L)).thenReturn(Optional.of(user));

    fixtures.service.deleteUser(11L);

    verify(fixtures.refreshTokenStore).deleteByUsername("bob");
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

    verify(fixtures.refreshTokenStore).deleteByUsername("charlie");
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

    verify(fixtures.refreshTokenStore, never()).deleteByUsername("diana");
  }

  private Fixtures fixtures() {
    var userRepository = mock(UserRepository.class);
    var roleRepository = mock(RoleRepository.class);
    var passwordHasher = mock(PasswordHasher.class);
    var mapper = mock(ViewMapper.class);
    var authorityVersionService = mock(AuthorityVersionService.class);
    var operationLogService = mock(OperationLogService.class);
    var authorizationService = mock(AuthorizationService.class);
    var currentUserAccess = mock(CurrentUserAccess.class);
    var permissionSnapshotStore = mock(PermissionSnapshotStore.class);
    var refreshTokenStore = mock(RefreshTokenStore.class);

    when(userRepository.findByIdOptional(anyLong())).thenReturn(Optional.empty());

    var service =
        new UserApplicationService(
            userRepository,
            roleRepository,
            passwordHasher,
            mapper,
            authorityVersionService,
            operationLogService,
            authorizationService,
            currentUserAccess,
            permissionSnapshotStore,
            refreshTokenStore);
    return new Fixtures(
        service,
        userRepository,
        passwordHasher,
        currentUserAccess,
        refreshTokenStore);
  }

  private record Fixtures(
      UserApplicationService service,
      UserRepository userRepository,
      PasswordHasher passwordHasher,
      CurrentUserAccess currentUserAccess,
      RefreshTokenStore refreshTokenStore) {}
}
