package com.github.DaiYuANg.security;

import java.util.Optional;

public interface CurrentAuthenticatedUserProvider {
    Optional<CurrentAuthenticatedUser> getCurrentUser();
}
