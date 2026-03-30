package com.github.DaiYuANg.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import lombok.NonNull;
import lombok.val;

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
    val current = valueCommands.get(KEY);
    var version = current;
    if (version == null || version.isBlank()) {
      version = Instant.now().toString();
      valueCommands.set(KEY, version);
    }
    return version;
  }

  public String bumpGlobalVersion() {
    val version = Instant.now().toString();
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
