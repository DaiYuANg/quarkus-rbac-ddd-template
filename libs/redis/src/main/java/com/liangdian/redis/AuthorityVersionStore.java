package com.liangdian.redis;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;

@ApplicationScoped
public class AuthorityVersionStore {
    private static final String KEY = "auth.authority:global-version";
    private final ValueCommands<String, String> values;
    private final KeyCommands<String> keys;

    @Inject
    public AuthorityVersionStore(RedisDataSource dataSource) {
        this.values = dataSource.value(String.class);
        this.keys = dataSource.key();
    }

    public String currentVersion() {
        var value = values.get(KEY);
        if (value == null || value.isBlank()) {
            value = Instant.now().toString();
            values.set(KEY, value);
        }
        return value;
    }

    public String bumpGlobalVersion() {
        var value = Instant.now().toString();
        values.set(KEY, value);
        return value;
    }

    public void clear() {
        keys.del(KEY);
    }

    public String versionFor(String username) {
        return currentVersion();
    }
}
