package com.github.DaiYuANg.redis;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class LoginAttemptStore {
    private final ValueCommands<String, String> commands;
    private final KeyCommands<String> keyCommands;

    @Inject
    public LoginAttemptStore(RedisDataSource dataSource) {
        this.commands = dataSource.value(String.class);
        this.keyCommands = dataSource.key();
    }

    public boolean isLocked(String username) {
        var until = lockedUntil(username);
        return until.filter(Instant.now()::isBefore).isPresent();
    }

    public Optional<Instant> lockedUntil(String username) {
        var raw = commands.get(lockKey(username));
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(raw));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public long incrementFailure(String username, Duration ttl) {
        var key = failureKey(username);
        var current = commands.get(key);
        long next = current == null ? 1 : Long.parseLong(current) + 1;
        commands.setex(key, ttl.getSeconds(), Long.toString(next));
        return next;
    }

    public void lock(String username, Duration ttl) {
        commands.setex(lockKey(username), ttl.getSeconds(), Instant.now().plus(ttl).toString());
    }

    public void clear(String username) {
        keyCommands.del(failureKey(username));
        keyCommands.del(lockKey(username));
    }

    private String failureKey(String username) {
        return "auth:login:failure:" + username;
    }

    private String lockKey(String username) {
        return "auth:login:lock:" + username;
    }
}
