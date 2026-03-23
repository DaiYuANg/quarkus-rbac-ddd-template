package com.github.DaiYuANg.cache;

import io.quarkus.infinispan.client.Remote;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import org.infinispan.client.hotrod.RemoteCache;

@ApplicationScoped
public class AuthorityVersionStore {
    private static final String CACHE_NAME = "rbac-auth";
    private static final String KEY = "auth.authority:global-version";

    private final RemoteCache<String, CacheValue> cache;

    @Inject
    public AuthorityVersionStore(@Remote(CACHE_NAME) RemoteCache<String, CacheValue> cache) {
        this.cache = cache;
    }

    public String currentVersion() {
        var value = cache.get(KEY);
        String version;
        if (value == null || value.data() == null || value.data().isBlank()) {
            version = Instant.now().toString();
            cache.put(KEY, new CacheValue(version));
        } else {
            version = value.data();
        }
        return version;
    }

    public String bumpGlobalVersion() {
        var version = Instant.now().toString();
        cache.put(KEY, new CacheValue(version));
        return version;
    }

    public void clear() {
        cache.remove(KEY);
    }

    public String versionFor(String username) {
        return currentVersion();
    }
}
