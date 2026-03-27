package com.github.DaiYuANg.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;

@ApplicationScoped
public class RefreshTokenStore {
  public record RefreshTokenOwner(Long userId, String username) {}

  private static final String OWNER_SEPARATOR = "\t";

  private final ValueCommands<String, String> valueCommands;
  private final SetCommands<String, String> setCommands;
  private final KeyCommands<String> keyCommands;

  public RefreshTokenStore(RedisDataSource ds) {
    this.valueCommands = ds.value(String.class);
    this.setCommands = ds.set(String.class);
    this.keyCommands = ds.key();
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
        .filter(username -> username != null && !username.isBlank());
  }

  public Optional<RefreshTokenOwner> getOwner(String refreshToken) {
    return parseOwner(valueCommands.get(key(refreshToken)));
  }

  public void delete(String refreshToken) {
    var owner = getOwner(refreshToken).orElse(null);
    keyCommands.del(key(refreshToken));
    cleanupMembership(owner, refreshToken);
  }

  public void deleteByUsername(String username) {
    if (username == null || username.isBlank()) {
      return;
    }
    for (String token : copyTokens(setCommands.smembers(userKey(username)))) {
      if (token != null && !token.isBlank()) {
        delete(token);
      }
    }
    keyCommands.del(userKey(username));
  }

  public void deleteByUserId(Long userId) {
    if (userId == null) {
      return;
    }
    for (String token : copyTokens(setCommands.smembers(userIdKey(userId)))) {
      if (token != null && !token.isBlank()) {
        delete(token);
      }
    }
    keyCommands.del(userIdKey(userId));
  }

  private String key(String refreshToken) {
    return "rbac-auth:refresh:" + refreshToken;
  }

  private String userKey(String username) {
    return "rbac-auth:refresh:user:" + username;
  }

  private String userIdKey(Long userId) {
    return "rbac-auth:refresh:user-id:" + userId;
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
    var remainingTokens = setCommands.smembers(membershipKey);
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
    var parts = rawValue.split(OWNER_SEPARATOR, 2);
    var userId = parseUserId(parts[0]);
    var username = parts.length < 2 || parts[1].isBlank() ? null : parts[1];
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
