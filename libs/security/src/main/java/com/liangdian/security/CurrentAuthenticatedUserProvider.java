package com.liangdian.security;

import java.util.Optional;

public interface CurrentAuthenticatedUserProvider {
    Optional<CurrentAuthenticatedUser> getCurrentUser();
}
