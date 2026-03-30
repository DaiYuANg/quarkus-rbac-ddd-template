package com.github.DaiYuANg.modules.identity.application.profile.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.identity.application.profile.DbUserProfileProvider;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DbUserProfileProviderTest {

  @Test
  void buildProfileThrowsWhenUserMissingInDatabase() {
    var repo = mock(UserRepository.class);
    when(repo.findByUsername("ghost")).thenReturn(Optional.empty());
    var provider = new DbUserProfileProvider(repo, mock(AuthorityVersionStore.class));
    var current = new CurrentAuthenticatedUser("ghost", "G", "ADMIN", Set.of(), Set.of(), Map.of());
    var ex = assertThrows(BizException.class, () -> provider.buildProfile(current));
    assertEquals(ResultCode.DATA_NOT_FOUND, ex.getResultCode());
  }

  @Test
  void supportsNonSuperAdminUserTypes() {
    var provider =
        new DbUserProfileProvider(mock(UserRepository.class), mock(AuthorityVersionStore.class));
    assertEquals(
        true,
        provider.supports(
            new CurrentAuthenticatedUser("u", "U", "ADMIN", Set.of(), Set.of(), Map.of())));
    assertEquals(
        false,
        provider.supports(
            new CurrentAuthenticatedUser("u", "U", "SUPER_ADMIN", Set.of(), Set.of(), Map.of())));
  }
}
