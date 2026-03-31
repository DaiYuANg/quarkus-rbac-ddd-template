package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.config.AuthCacheKeyConfig;
import com.google.common.base.Strings;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

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

  public void save(@NonNull String refreshToken, Long userId, String username, @NonNull Duration ttl) {
    valueCommands.setex(key(refreshToken), (int) ttl.toSeconds(), encodeOwner(userId, username));
    if (userId != null) {
      setCommands.sadd(userIdKey(userId), refreshToken);
    }
    val normalizedUsername = normalize(username);
    if (normalizedUsername != null) {
      setCommands.sadd(userKey(normalizedUsername), refreshToken);
    }
  }

  public Optional<String> getUsername(@NonNull String refreshToken) {
    return getOwner(refreshToken).map(RefreshTokenOwner::username).map(this::normalize);
  }

  public Optional<RefreshTokenOwner> getOwner(@NonNull String refreshToken) {
    return parseOwner(valueCommands.get(key(refreshToken)));
  }

  public void delete(@NonNull String refreshToken) {
    val owner = getOwner(refreshToken).orElse(null);
    keyCommands.del(key(refreshToken));
    cleanupMembership(owner, refreshToken);
  }

  public void deleteByUsername(String username) {
    val normalizedUsername = normalize(username);
    if (normalizedUsername == null) {
      return;
    }
    copyTokens(setCommands.smembers(userKey(normalizedUsername))).stream()
        .map(this::normalize)
        .filter(Objects::nonNull)
        .forEach(this::delete);
    keyCommands.del(userKey(normalizedUsername));
  }

  public void deleteByUserId(Long userId) {
    if (userId == null) {
      return;
    }
    copyTokens(setCommands.smembers(userIdKey(userId))).stream()
        .map(this::normalize)
        .filter(Objects::nonNull)
        .forEach(this::delete);
    keyCommands.del(userIdKey(userId));
  }

  private String key(@NonNull String refreshToken) {
    return authCacheKeyConfig.refreshTokenKey(refreshToken);
  }

  private String userKey(@NonNull String username) {
    return authCacheKeyConfig.refreshUserKey(username);
  }

  private String userIdKey(@NonNull Long userId) {
    return authCacheKeyConfig.refreshUserIdKey(userId);
  }

  private void cleanupMembership(RefreshTokenOwner owner, @NonNull String refreshToken) {
    if (owner == null) {
      return;
    }
    if (owner.userId() != null) {
      removeTokenFromSet(userIdKey(owner.userId()), refreshToken);
    }
    val normalizedUsername = normalize(owner.username());
    if (normalizedUsername != null) {
      removeTokenFromSet(userKey(normalizedUsername), refreshToken);
    }
  }

  private void removeTokenFromSet(@NonNull String membershipKey, @NonNull String refreshToken) {
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
    return Strings.nullToEmpty(Objects.toString(userId, null))
        + OWNER_SEPARATOR
        + Strings.nullToEmpty(normalize(username));
  }

  private Optional<RefreshTokenOwner> parseOwner(String rawValue) {
    val normalizedValue = normalize(rawValue);
    if (normalizedValue == null) {
      return Optional.empty();
    }
    if (!normalizedValue.contains(OWNER_SEPARATOR)) {
      return Optional.of(new RefreshTokenOwner(null, normalizedValue));
    }
    val parts = normalizedValue.split(OWNER_SEPARATOR, 2);
    val userId = parseUserId(parts[0]);
    val username = parts.length < 2 ? null : normalize(parts[1]);
    if (userId == null && username == null) {
      return Optional.empty();
    }
    return Optional.of(new RefreshTokenOwner(userId, username));
  }

  private Long parseUserId(String rawValue) {
    val normalizedValue = normalize(rawValue);
    if (normalizedValue == null) {
      return null;
    }
    try {
      return Long.parseLong(normalizedValue);
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
