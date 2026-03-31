package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.config.AuthCacheKeyConfig;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

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
    val version = StringUtils.trimToNull(current);
    if (version != null) {
      return version;
    }
    val newVersion = Instant.now().toString();
    valueCommands.set(authCacheKeyConfig.authorityVersionKey(), newVersion);
    return newVersion;
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
