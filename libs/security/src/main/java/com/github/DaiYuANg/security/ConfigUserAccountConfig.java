package com.github.DaiYuANg.security;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ConfigMapping(prefix = "app.security")
public interface ConfigUserAccountConfig {
    @WithName("config-users")
    Map<String, ConfigUser> users();

    interface ConfigUser {
        String username();
        String passwordHash();
        Optional<String> displayName();
        Optional<List<String>> roles();
        Optional<List<String>> permissions();
    }
}
