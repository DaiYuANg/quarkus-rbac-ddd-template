package com.github.DaiYuANg.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

@ApplicationScoped
public class RefreshTokenStore {

    private final ValueCommands<String, String> valueCommands;
    private final KeyCommands<String> keyCommands;

    public RefreshTokenStore(RedisDataSource ds) {
        this.valueCommands = ds.value(String.class);
        this.keyCommands = ds.key();
    }

    public void save(String refreshToken, String username, @NonNull Duration ttl) {
        valueCommands.setex(key(refreshToken), (int) ttl.toSeconds(), username);
    }

    public Optional<String> getUsername(String refreshToken) {
        var value = valueCommands.get(key(refreshToken));
        return value != null ? Optional.of(value) : Optional.empty();
    }

    public void delete(String refreshToken) {
        keyCommands.del(key(refreshToken));
    }

    private String key(String refreshToken) {
        return "rbac-auth:refresh:" + refreshToken;
    }
}
