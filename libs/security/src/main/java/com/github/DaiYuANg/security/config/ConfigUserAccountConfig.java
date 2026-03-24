package com.github.DaiYuANg.security.config;

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
        @WithName("password-hash")
        String passwordHash();
        Optional<String> displayName();
        Optional<List<String>> roles();
        Optional<List<String>> permissions();
    }
}
