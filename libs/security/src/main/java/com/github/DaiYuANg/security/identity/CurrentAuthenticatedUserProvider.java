package com.github.DaiYuANg.security.identity;

import java.util.Optional;

public interface CurrentAuthenticatedUserProvider {
    Optional<CurrentAuthenticatedUser> getCurrentUser();
}
