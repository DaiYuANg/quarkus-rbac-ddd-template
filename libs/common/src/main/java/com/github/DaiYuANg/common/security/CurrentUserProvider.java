package com.github.DaiYuANg.common.security;

import java.util.Optional;

public interface CurrentUserProvider {
    Optional<String> currentUsername();
}
