package com.github.DaiYuANg.modules.identity.application.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.audit.support.AuditSnapshotProvider;
import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.cache.LoginAttemptStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.identity.application.AuthApplicationService;
import com.github.DaiYuANg.modules.identity.application.LoginAuditEvent;
import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationTokenBuilder;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVoBuilder;
import com.github.DaiYuANg.modules.identity.application.profile.UserProfileResolutionService;
import com.github.DaiYuANg.modules.security.runtime.auth.AdminAuthenticationLifecycle;
import com.github.DaiYuANg.modules.security.runtime.auth.AdminTokenIssuer;
import com.github.DaiYuANg.security.access.CurrentUserAccess;
import com.github.DaiYuANg.security.auth.AuthenticationResult;
import com.github.DaiYuANg.security.auth.LoginAuthenticationManager;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.User;
import com.github.DaiYuANg.security.config.AuthSecurityConfig;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import jakarta.enterprise.event.Event;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AuthApplicationServiceBehaviorTest {

  @SuppressWarnings("unchecked")
  private static Event<LoginAuditEvent> loginEventMock() {
    return (Event<LoginAuditEvent>) mock(Event.class);
  }

  @Test
  void refreshRevokesOldTokenBeforeIssuingNewToken() {
    var user =
        new AuthenticatedUser(
            "alice", "Alice", "ADMIN", Set.of("admin"), Set.of(User.VIEW), Map.of(), 1L);
    var authResult = new AuthenticationResult(user, "refresh-token");
    var issued =
        SystemAuthenticationTokenBuilder.builder()
            .accessToken("access")
            .refreshToken("refresh-new")
            .tokenType("Bearer")
            .expiresIn(120L)
            .authorityVersion("v1")
            .build();

    var userRepository = mock(UserRepository.class);
    var authenticationManager = mock(LoginAuthenticationManager.class);
    var tokenIssuer = mock(AdminTokenIssuer.class);
    var loginAttemptStore = mock(LoginAttemptStore.class);
    var authorityVersionStore = mock(AuthorityVersionStore.class);
    var loginAuditEvent = loginEventMock();
    var auditSnapshotProvider = mock(AuditSnapshotProvider.class);
    var authSecurityConfig = mock(AuthSecurityConfig.class);
    var lifecycle = mock(AdminAuthenticationLifecycle.class);
    var currentUserAccess = mock(CurrentUserAccess.class);
    var userProfileResolutionService = mock(UserProfileResolutionService.class);

    when(authenticationManager.authenticate(any())).thenReturn(authResult);
    when(tokenIssuer.issue(user)).thenReturn(issued);

    var service =
        new AuthApplicationService(
            userRepository,
            authenticationManager,
            tokenIssuer,
            loginAttemptStore,
            authorityVersionStore,
            loginAuditEvent,
            auditSnapshotProvider,
            authSecurityConfig,
            lifecycle,
            currentUserAccess,
            userProfileResolutionService);

    var actual = service.refreshToken("refresh-old");

    assertSame(issued, actual);
    var inOrder = inOrder(lifecycle, tokenIssuer);
    inOrder.verify(lifecycle).revokeRefreshToken("refresh-old");
    inOrder.verify(lifecycle).publishSnapshot(user);
    inOrder.verify(tokenIssuer).issue(user);
  }

  @Test
  void profileDelegatesToResolutionWhenSubjectMatchesPath() {
    var current =
        new CurrentAuthenticatedUser("alice", "Alice", "ADMIN", Set.of(), Set.of(), Map.of());
    var currentUserAccess = mock(CurrentUserAccess.class);
    when(currentUserAccess.currentUser()).thenReturn(Optional.of(current));
    var expected =
        UserDetailVoBuilder.builder()
            .userid(1L)
            .username("alice")
            .nickname("Alice")
            .permissions(Set.of())
            .roleCodes(Set.of())
            .authorityKey("k")
            .build();
    var userProfileResolutionService = mock(UserProfileResolutionService.class);
    when(userProfileResolutionService.resolve(current)).thenReturn(expected);

    var service =
        new AuthApplicationService(
            mock(UserRepository.class),
            mock(LoginAuthenticationManager.class),
            mock(AdminTokenIssuer.class),
            mock(LoginAttemptStore.class),
            mock(AuthorityVersionStore.class),
            loginEventMock(),
            mock(AuditSnapshotProvider.class),
            mock(AuthSecurityConfig.class),
            mock(AdminAuthenticationLifecycle.class),
            currentUserAccess,
            userProfileResolutionService);

    assertSame(expected, service.profile("alice"));
  }

  @Test
  void profileForbiddenWhenUsernameDoesNotMatchCurrentSubject() {
    var current = new CurrentAuthenticatedUser("alice", "A", "ADMIN", Set.of(), Set.of(), Map.of());
    var currentUserAccess = mock(CurrentUserAccess.class);
    when(currentUserAccess.currentUser()).thenReturn(Optional.of(current));

    var service =
        new AuthApplicationService(
            mock(UserRepository.class),
            mock(LoginAuthenticationManager.class),
            mock(AdminTokenIssuer.class),
            mock(LoginAttemptStore.class),
            mock(AuthorityVersionStore.class),
            loginEventMock(),
            mock(AuditSnapshotProvider.class),
            mock(AuthSecurityConfig.class),
            mock(AdminAuthenticationLifecycle.class),
            currentUserAccess,
            mock(UserProfileResolutionService.class));

    var ex = assertThrows(BizException.class, () -> service.profile("bob"));
    assertEquals(ResultCode.FORBIDDEN, ex.getResultCode());
  }

  @Test
  void profileUnauthorizedWhenNoCurrentUser() {
    var currentUserAccess = mock(CurrentUserAccess.class);
    when(currentUserAccess.currentUser()).thenReturn(Optional.empty());

    var service =
        new AuthApplicationService(
            mock(UserRepository.class),
            mock(LoginAuthenticationManager.class),
            mock(AdminTokenIssuer.class),
            mock(LoginAttemptStore.class),
            mock(AuthorityVersionStore.class),
            loginEventMock(),
            mock(AuditSnapshotProvider.class),
            mock(AuthSecurityConfig.class),
            mock(AdminAuthenticationLifecycle.class),
            currentUserAccess,
            mock(UserProfileResolutionService.class));

    var ex = assertThrows(BizException.class, () -> service.profile("any"));
    assertEquals(ResultCode.UNAUTHORIZED, ex.getResultCode());
  }
}
