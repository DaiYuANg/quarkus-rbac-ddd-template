package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.config.AuthCacheKeyConfig;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.val;

@ApplicationScoped
public class RefreshTokenStore {
  public record RefreshTokenOwner(Long userId, String username) {}

  private static final String OWNER_SEPARATOR = "\t";

  private final ValueCommands<String, String> valueCommands;
  private final SetCommands<String, String> setCommands;
  private final KeyCommands<String> keyCommands;
  private final AuthCacheKeyConfig authCacheKeyConfig;

  public RefreshTokenStore(@NonNull RedisDataSource ds, @NonNull AuthCacheKeyConfig authCacheKeyConfig) {
    this.valueCommands = ds.value(String.class);
    this.setCommands = ds.set(String.class);
    this.keyCommands = ds.key();
    this.authCacheKeyConfig = authCacheKeyConfig;
  }

  public void save(String refreshToken, Long userId, String username, @NonNull Duration ttl) {
    valueCommands.setex(key(refreshToken), (int) ttl.toSeconds(), encodeOwner(userId, username));
    if (userId != null) {
      setCommands.sadd(userIdKey(userId), refreshToken);
    }
    if (username != null && !username.isBlank()) {
      setCommands.sadd(userKey(username), refreshToken);
    }
  }

  public Optional<String> getUsername(String refreshToken) {
    return getOwner(refreshToken)
        .map(RefreshTokenOwner::username)
        .filter(username -> !username.isBlank());
  }

  public Optional<RefreshTokenOwner> getOwner(String refreshToken) {
    return parseOwner(valueCommands.get(key(refreshToken)));
  }

  public void delete(String refreshToken) {
    val owner = getOwner(refreshToken).orElse(null);
    keyCommands.del(key(refreshToken));
    cleanupMembership(owner, refreshToken);
  }

  public void deleteByUsername(String username) {
    if (username == null || username.isBlank()) {
      return;
    }
    copyTokens(setCommands.smembers(userKey(username))).stream()
        .filter(token -> token != null && !token.isBlank())
        .forEach(this::delete);
    keyCommands.del(userKey(username));
  }

  public void deleteByUserId(Long userId) {
    if (userId == null) {
      return;
    }
    copyTokens(setCommands.smembers(userIdKey(userId))).stream()
        .filter(token -> token != null && !token.isBlank())
        .forEach(this::delete);
    keyCommands.del(userIdKey(userId));
  }

  private String key(String refreshToken) {
    return authCacheKeyConfig.refreshTokenKey(refreshToken);
  }

  private String userKey(String username) {
    return authCacheKeyConfig.refreshUserKey(username);
  }

  private String userIdKey(Long userId) {
    return authCacheKeyConfig.refreshUserIdKey(userId);
  }

  private void cleanupMembership(RefreshTokenOwner owner, String refreshToken) {
    if (owner == null) {
      return;
    }
    if (owner.userId() != null) {
      removeTokenFromSet(userIdKey(owner.userId()), refreshToken);
    }
    if (owner.username() != null && !owner.username().isBlank()) {
      removeTokenFromSet(userKey(owner.username()), refreshToken);
    }
  }

  private void removeTokenFromSet(String membershipKey, String refreshToken) {
    setCommands.srem(membershipKey, refreshToken);
    val remainingTokens = setCommands.smembers(membershipKey);
    if (remainingTokens == null || remainingTokens.isEmpty()) {
      keyCommands.del(membershipKey);
    }
  }

  private LinkedHashSet<String> copyTokens(Set<String> tokens) {
    return tokens == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tokens);
  }

  private String encodeOwner(Long userId, String username) {
    return (userId == null ? "" : String.valueOf(userId))
        + OWNER_SEPARATOR
        + (username == null ? "" : username);
  }

  private Optional<RefreshTokenOwner> parseOwner(String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      return Optional.empty();
    }
    if (!rawValue.contains(OWNER_SEPARATOR)) {
      return Optional.of(new RefreshTokenOwner(null, rawValue));
    }
    val parts = rawValue.split(OWNER_SEPARATOR, 2);
    val userId = parseUserId(parts[0]);
    val username = parts.length < 2 || parts[1].isBlank() ? null : parts[1];
    if (userId == null && username == null) {
      return Optional.empty();
    }
    return Optional.of(new RefreshTokenOwner(userId, username));
  }

  private Long parseUserId(String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(rawValue.trim());
    } catch (NumberFormatException ignored) {
      return null;
    }
  }
}
