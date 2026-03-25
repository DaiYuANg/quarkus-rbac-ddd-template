package com.github.DaiYuANg.security.token;

import com.github.DaiYuANg.security.identity.AuthenticatedUser;

public interface TokenIssuer<T> {
  T issue(AuthenticatedUser user);
}
