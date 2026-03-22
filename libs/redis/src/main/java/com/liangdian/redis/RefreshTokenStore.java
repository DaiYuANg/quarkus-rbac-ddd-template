package com.liangdian.redis;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Optional;

@ApplicationScoped
public class RefreshTokenStore {
    private final ValueCommands<String, String> commands;
    private final KeyCommands<String> keyCommands;

    @Inject
    public RefreshTokenStore(RedisDataSource dataSource) {
        this.commands = dataSource.value(String.class);
        this.keyCommands = dataSource.key();
    }

    public void save(String refreshToken, String username, Duration ttl) {
        commands.setex(key(refreshToken), ttl.getSeconds(), username);
    }

    public Optional<String> getUsername(String refreshToken) {
        return Optional.ofNullable(commands.get(key(refreshToken)));
    }

    public void delete(String refreshToken) {
        keyCommands.del(key(refreshToken));
    }

    private String key(String refreshToken) {
        return "auth:refresh:" + refreshToken;
    }
}
