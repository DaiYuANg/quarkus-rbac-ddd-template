package com.github.DaiYuANg.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.jspecify.annotations.NonNull;

import java.time.Instant;

@ApplicationScoped
public class AuthorityVersionStore {

  private static final String KEY = "rbac-auth:authority:global-version";

  private final ValueCommands<String, String> valueCommands;
  private final KeyCommands<String> keyCommands;

  public AuthorityVersionStore(@NonNull RedisDataSource ds) {
    this.valueCommands = ds.value(String.class);
    this.keyCommands = ds.key();
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
}
