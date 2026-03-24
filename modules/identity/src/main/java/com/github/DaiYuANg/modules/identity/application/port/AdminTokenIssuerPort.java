package com.github.DaiYuANg.modules.identity.application.port;

import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;

public interface AdminTokenIssuerPort {
  SystemAuthenticationToken issue(AuthenticatedUser user);
}
