package com.github.DaiYuANg.modules.identity.application.port;

import com.github.DaiYuANg.security.identity.AuthenticatedUser;

public interface AuthenticationLifecyclePort {
  void publishSnapshot(AuthenticatedUser user);

  void revokeRefreshToken(String refreshToken);
}
