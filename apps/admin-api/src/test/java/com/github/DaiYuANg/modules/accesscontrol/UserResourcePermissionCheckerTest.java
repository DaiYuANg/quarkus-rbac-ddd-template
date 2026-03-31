package com.github.DaiYuANg.modules.accesscontrol;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.modules.accesscontrol.application.user.UserApplicationService;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.val;
import org.junit.jupiter.api.Test;

class UserResourcePermissionCheckerTest {

  @Test
  void selfPasswordPermissionMatchesCurrentUserId() {
    val resource = new UserResource(mock(UserApplicationService.class));
    val identity = mock(SecurityIdentity.class);
    when(identity.getAttribute(PrincipalAttributeKeys.USER_ID)).thenReturn(42L);

    assertTrue(resource.canChangeOwnPassword(identity, 42L));
    assertFalse(resource.canChangeOwnPassword(identity, 43L));
  }

  @Test
  void selfPasswordPermissionParsesStringUserId() {
    val resource = new UserResource(mock(UserApplicationService.class));
    val identity = mock(SecurityIdentity.class);
    when(identity.getAttribute(PrincipalAttributeKeys.USER_ID)).thenReturn("99");

    assertTrue(resource.canChangeOwnPassword(identity, 99L));
    assertFalse(resource.canChangeOwnPassword(identity, 100L));
  }
}
