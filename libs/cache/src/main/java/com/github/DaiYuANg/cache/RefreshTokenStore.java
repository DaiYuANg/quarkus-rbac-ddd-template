package com.github.DaiYuANg.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;

@ApplicationScoped
public class RefreshTokenStore {

    private final ValueCommands<String, String> valueCommands;
    private final SetCommands<String, String> setCommands;
    private final KeyCommands<String> keyCommands;

    public RefreshTokenStore(RedisDataSource ds) {
        this.valueCommands = ds.value(String.class);
        this.setCommands = ds.set(String.class);
        this.keyCommands = ds.key();
    }

    public void save(String refreshToken, String username, @NonNull Duration ttl) {
        valueCommands.setex(key(refreshToken), (int) ttl.toSeconds(), username);
        setCommands.sadd(userKey(username), refreshToken);
    }

    public Optional<String> getUsername(String refreshToken) {
        var value = valueCommands.get(key(refreshToken));
        return value != null ? Optional.of(value) : Optional.empty();
    }

    public void delete(String refreshToken) {
        var username = getUsername(refreshToken).orElse(null);
        keyCommands.del(key(refreshToken));
        if (username != null && !username.isBlank()) {
            var userKey = userKey(username);
            setCommands.srem(userKey, refreshToken);
            Set<String> remains = setCommands.smembers(userKey);
            if (remains.isEmpty()) {
                keyCommands.del(userKey);
            }
        }
    }

    public void deleteByUsername(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        var userKey = userKey(username);
        Set<String> tokens = setCommands.smembers(userKey);
        for (String token : tokens) {
            if (token != null && !token.isBlank()) {
                keyCommands.del(key(token));
            }
        }
        keyCommands.del(userKey);
    }

    private String key(String refreshToken) {
        return "rbac-auth:refresh:" + refreshToken;
    }

    private String userKey(String username) {
        return "rbac-auth:refresh:user:" + username;
    }
}
