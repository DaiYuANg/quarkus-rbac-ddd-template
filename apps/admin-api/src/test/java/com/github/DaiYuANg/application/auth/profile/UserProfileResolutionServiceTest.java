package com.github.DaiYuANg.application.auth.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.api.dto.response.UserDetailVo;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import jakarta.enterprise.inject.Instance;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserProfileResolutionServiceTest {

  @Test
  void picksFirstSupportingProviderByOrder() {
    var p100 = mock(UserProfileProvider.class);
    var p200 = mock(UserProfileProvider.class);
    when(p100.order()).thenReturn(100);
    when(p200.order()).thenReturn(200);
    var current =
        new CurrentAuthenticatedUser("u", "U", "ADMIN", Set.of(), Set.of(), Map.of());
    when(p100.supports(current)).thenReturn(false);
    when(p200.supports(current)).thenReturn(true);
    var expected = new UserDetailVo(1L, "u", "U", Set.of(), Set.of(), "k");
    when(p200.buildProfile(current)).thenReturn(expected);

    @SuppressWarnings("unchecked")
    Instance<UserProfileProvider> instance = mock(Instance.class);
    when(instance.spliterator()).thenAnswer(inv -> List.of(p200, p100).spliterator());

    var service = new UserProfileResolutionService(instance);
    assertEquals(expected, service.resolve(current));
  }

  @Test
  void throwsWhenNoProviderSupports() {
    var p = mock(UserProfileProvider.class);
    when(p.order()).thenReturn(0);
    when(p.supports(Mockito.any())).thenReturn(false);
    @SuppressWarnings("unchecked")
    Instance<UserProfileProvider> instance = mock(Instance.class);
    when(instance.spliterator()).thenAnswer(inv -> List.of(p).spliterator());
    var service = new UserProfileResolutionService(instance);
    var ex =
        assertThrows(
            BizException.class,
            () ->
                service.resolve(
                    new CurrentAuthenticatedUser("x", "X", "OTHER", Set.of(), Set.of(), Map.of())));
    assertEquals(ResultCode.DATA_NOT_FOUND, ex.getResultCode());
  }
}
