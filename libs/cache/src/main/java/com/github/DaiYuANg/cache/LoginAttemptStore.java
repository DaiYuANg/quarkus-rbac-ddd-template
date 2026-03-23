package com.github.DaiYuANg.cache;

import io.quarkus.infinispan.client.Remote;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.infinispan.client.hotrod.RemoteCache;

@ApplicationScoped
public class LoginAttemptStore {
    private static final String CACHE_NAME = "rbac-auth";

    private final RemoteCache<String, CacheValue> cache;

    @Inject
    public LoginAttemptStore(@Remote(CACHE_NAME) RemoteCache<String, CacheValue> cache) {
        this.cache = cache;
    }

    public boolean isLocked(String username) {
        var until = lockedUntil(username);
        return until.filter(Instant.now()::isBefore).isPresent();
    }

    public Optional<Instant> lockedUntil(String username) {
        var value = cache.get(lockKey(username));
        if (value == null || value.data() == null || value.data().isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(value.data()));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public long incrementFailure(String username, Duration ttl) {
        var key = failureKey(username);
        var value = cache.get(key);
        long next = value == null || value.data() == null ? 1 : Long.parseLong(value.data()) + 1;
        cache.put(key, new CacheValue(Long.toString(next)), ttl.toSeconds(), TimeUnit.SECONDS);
        return next;
    }

    public void lock(String username, Duration ttl) {
        cache.put(lockKey(username),
            new CacheValue(Instant.now().plus(ttl).toString()),
            ttl.toSeconds(),
            TimeUnit.SECONDS);
    }

    public void clear(String username) {
        cache.remove(failureKey(username));
        cache.remove(lockKey(username));
    }

    private String failureKey(String username) {
        return "auth:login:failure:" + username;
    }

    private String lockKey(String username) {
        return "auth:login:lock:" + username;
    }
}
