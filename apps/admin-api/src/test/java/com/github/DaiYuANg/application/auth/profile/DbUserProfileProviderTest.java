package com.github.DaiYuANg.application.auth.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.application.converter.ViewMapper;
import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.security.CurrentAuthenticatedUser;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DbUserProfileProviderTest {

  @Test
  void buildProfileThrowsWhenUserMissingInDatabase() {
    var repo = mock(UserRepository.class);
    when(repo.findByUsername("ghost")).thenReturn(Optional.empty());
    var provider =
        new DbUserProfileProvider(repo, mock(ViewMapper.class), mock(AuthorityVersionStore.class));
    var current =
        new CurrentAuthenticatedUser("ghost", "G", "ADMIN", Set.of(), Set.of(), Map.of());
    var ex =
        assertThrows(BizException.class, () -> provider.buildProfile(current));
    assertEquals(ResultCode.DATA_NOT_FOUND, ex.getResultCode());
  }

  @Test
  void supportsNonConfigUserTypes() {
    var provider =
        new DbUserProfileProvider(
            mock(UserRepository.class), mock(ViewMapper.class), mock(AuthorityVersionStore.class));
    assertEquals(
        true,
        provider.supports(
            new CurrentAuthenticatedUser("u", "U", "ADMIN", Set.of(), Set.of(), Map.of())));
    assertEquals(
        false,
        provider.supports(
            new CurrentAuthenticatedUser("u", "U", "CONFIG", Set.of(), Set.of(), Map.of())));
  }
}
