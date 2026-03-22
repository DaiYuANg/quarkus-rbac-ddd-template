package com.liangdian.common.security;

import java.util.Optional;

public interface CurrentUserProvider {
    Optional<String> currentUsername();
}
