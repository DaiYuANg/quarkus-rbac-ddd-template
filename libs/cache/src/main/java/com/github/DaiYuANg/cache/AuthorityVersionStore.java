package com.github.DaiYuANg.cache;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;

@ApplicationScoped
public class AuthorityVersionStore {

    private static final String KEY = "rbac-auth:authority:global-version";

    private final ValueCommands<String, String> valueCommands;
    private final KeyCommands<String> keyCommands;
    private final ReactiveValueCommands<String, String> reactiveValueCommands;
    private final ReactiveKeyCommands<String> reactiveKeyCommands;

    public AuthorityVersionStore(RedisDataSource ds, ReactiveRedisDataSource reactiveDs) {
        this.valueCommands = ds.value(String.class);
        this.keyCommands = ds.key();
        this.reactiveValueCommands = reactiveDs.value(String.class);
        this.reactiveKeyCommands = reactiveDs.key();
    }

    public String currentVersion() {
        var version = valueCommands.get(KEY);
        if (version == null || version.isBlank()) {
            version = Instant.now().toString();
            valueCommands.set(KEY, version);
        }
        return version;
    }

    public String bumpGlobalVersion() {
        var version = Instant.now().toString();
        valueCommands.set(KEY, version);
        return version;
    }

    public void clear() {
        keyCommands.del(KEY);
    }

    public String versionFor(String username) {
        return currentVersion();
    }

    public Uni<String> currentVersionAsync() {
        return reactiveValueCommands.get(KEY).chain(version -> {
            if (version == null || version.isBlank()) {
                var freshVersion = Instant.now().toString();
                return reactiveValueCommands.set(KEY, freshVersion).replaceWith(freshVersion);
            }
            return Uni.createFrom().item(version);
        });
    }

    public Uni<String> versionForAsync(String username) {
        return currentVersionAsync();
    }

    public Uni<String> bumpGlobalVersionAsync() {
        var version = Instant.now().toString();
        return reactiveValueCommands.set(KEY, version).replaceWith(version);
    }

    public Uni<Void> clearAsync() {
        return reactiveKeyCommands.del(KEY).replaceWithVoid();
    }
}
