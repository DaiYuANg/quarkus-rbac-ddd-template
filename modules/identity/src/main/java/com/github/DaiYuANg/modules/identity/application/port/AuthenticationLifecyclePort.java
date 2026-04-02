package com.github.DaiYuANg.modules.identity.application.port;

import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import java.util.Optional;

public interface AuthenticationLifecyclePort {
  void publishSnapshot(AuthenticatedUser user);

  Optional<String> findRefreshTokenOwner(String refreshToken);

  void revokeRefreshToken(String refreshToken);
}
