package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.config.AuthCacheKeyConfig;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import lombok.NonNull;
import lombok.val;

@ApplicationScoped
public class AuthorityVersionStore {

  private final ValueCommands<String, String> valueCommands;
  private final KeyCommands<String> keyCommands;
  private final AuthCacheKeyConfig authCacheKeyConfig;

  public AuthorityVersionStore(@NonNull RedisDataSource ds, @NonNull AuthCacheKeyConfig authCacheKeyConfig) {
    this.valueCommands = ds.value(String.class);
    this.keyCommands = ds.key();
    this.authCacheKeyConfig = authCacheKeyConfig;
  }

  public String currentVersion() {
    val current = valueCommands.get(authCacheKeyConfig.authorityVersionKey());
    var version = current;
    if (version == null || version.isBlank()) {
      version = Instant.now().toString();
      valueCommands.set(authCacheKeyConfig.authorityVersionKey(), version);
    }
    return version;
  }

  public String bumpGlobalVersion() {
    val version = Instant.now().toString();
    valueCommands.set(authCacheKeyConfig.authorityVersionKey(), version);
    return version;
  }

  public void clear() {
    keyCommands.del(authCacheKeyConfig.authorityVersionKey());
  }

  public String versionFor(String username) {
    return currentVersion();
  }
}
