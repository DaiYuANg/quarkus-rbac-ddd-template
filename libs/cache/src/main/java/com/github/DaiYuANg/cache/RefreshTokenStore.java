package com.github.DaiYuANg.cache;

import io.quarkus.infinispan.client.Remote;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.infinispan.client.hotrod.RemoteCache;
import org.jspecify.annotations.NonNull;

@ApplicationScoped
public class RefreshTokenStore {
    private static final String CACHE_NAME = "rbac-auth";

    private final RemoteCache<String, CacheValue> cache;

    @Inject
    public RefreshTokenStore(@Remote(CACHE_NAME) RemoteCache<String, CacheValue> cache) {
        this.cache = cache;
    }

    public void save(String refreshToken, String username, @NonNull Duration ttl) {
        cache.put(key(refreshToken), new CacheValue(username), ttl.toSeconds(), TimeUnit.SECONDS);
    }

    public Optional<String> getUsername(String refreshToken) {
        var value = cache.get(key(refreshToken));
        return value != null && value.data() != null ? Optional.of(value.data()) : Optional.empty();
    }

    public void delete(String refreshToken) {
        cache.remove(key(refreshToken));
    }

    private String key(String refreshToken) {
        return "auth:refresh:" + refreshToken;
    }
}
