package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.config.AuthCacheKeyConfig;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.NonNull;

@ApplicationScoped
public class LoginAttemptStore {

  private final ValueCommands<String, String> valueCommands;
  private final KeyCommands<String> keyCommands;
  private final AuthCacheKeyConfig authCacheKeyConfig;

  public LoginAttemptStore(@NonNull RedisDataSource ds, @NonNull AuthCacheKeyConfig authCacheKeyConfig) {
    this.valueCommands = ds.value(String.class);
    this.keyCommands = ds.key();
    this.authCacheKeyConfig = authCacheKeyConfig;
  }

  public boolean isLocked(String username) {
    var until = lockedUntil(username);
    return until.filter(Instant.now()::isBefore).isPresent();
  }

  public Optional<Instant> lockedUntil(String username) {
    var value = valueCommands.get(lockKey(username));
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    try {
      return Optional.of(Instant.parse(value));
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  public long incrementFailure(String username, Duration ttl) {
    var key = failureKey(username);
    var value = valueCommands.get(key);
    long next = value == null || value.isBlank() ? 1 : Long.parseLong(value) + 1;
    valueCommands.setex(key, (int) ttl.toSeconds(), Long.toString(next));
    return next;
  }

  public void lock(String username, Duration ttl) {
    valueCommands.setex(
        lockKey(username), (int) ttl.toSeconds(), Instant.now().plus(ttl).toString());
  }

  public void clear(String username) {
    keyCommands.del(failureKey(username));
    keyCommands.del(lockKey(username));
  }

  private String failureKey(String username) {
    return authCacheKeyConfig.loginFailureKey(username);
  }

  private String lockKey(String username) {
    return authCacheKeyConfig.loginLockKey(username);
  }
}
