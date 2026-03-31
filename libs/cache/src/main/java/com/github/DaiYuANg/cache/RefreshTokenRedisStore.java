package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.config.AuthCacheKeyConfig;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.NonNull;
import lombok.val;

@ApplicationScoped
class RefreshTokenRedisStore {
  private final ValueCommands<String, String> valueCommands;
  private final SetCommands<String, String> setCommands;
  private final KeyCommands<String> keyCommands;
  private final AuthCacheKeyConfig authCacheKeyConfig;

  RefreshTokenRedisStore(@NonNull RedisDataSource ds, @NonNull AuthCacheKeyConfig authCacheKeyConfig) {
    this.valueCommands = ds.value(String.class);
    this.setCommands = ds.set(String.class);
    this.keyCommands = ds.key();
    this.authCacheKeyConfig = authCacheKeyConfig;
  }

  void save(
      @NonNull String refreshToken,
      @NonNull String encodedOwner,
      Long userId,
      String username,
      @NonNull Duration ttl) {
    valueCommands.setex(tokenKey(refreshToken), (int) ttl.toSeconds(), encodedOwner);
    if (userId != null) {
      setCommands.sadd(userIdKey(userId), refreshToken);
    }
    if (username != null) {
      setCommands.sadd(usernameKey(username), refreshToken);
    }
  }

  String getOwnerPayload(@NonNull String refreshToken) {
    return valueCommands.get(tokenKey(refreshToken));
  }

  void deleteToken(@NonNull String refreshToken) {
    keyCommands.del(tokenKey(refreshToken));
  }

  Set<String> findTokensByUsername(@NonNull String username) {
    return copyTokens(setCommands.smembers(usernameKey(username)));
  }

  Set<String> findTokensByUserId(@NonNull Long userId) {
    return copyTokens(setCommands.smembers(userIdKey(userId)));
  }

  void deleteUsernameIndex(@NonNull String username) {
    keyCommands.del(usernameKey(username));
  }

  void deleteUserIdIndex(@NonNull Long userId) {
    keyCommands.del(userIdKey(userId));
  }

  void cleanupMembership(RefreshTokenStore.RefreshTokenOwner owner, @NonNull String refreshToken) {
    if (owner == null) {
      return;
    }
    if (owner.userId() != null) {
      removeTokenFromSet(userIdKey(owner.userId()), refreshToken);
    }
    val normalizedUsername = RefreshTokenOwnerCodec.normalize(owner.username());
    if (normalizedUsername != null) {
      removeTokenFromSet(usernameKey(normalizedUsername), refreshToken);
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

  private String tokenKey(@NonNull String refreshToken) {
    return authCacheKeyConfig.refreshTokenKey(refreshToken);
  }

  private String usernameKey(@NonNull String username) {
    return authCacheKeyConfig.refreshUserKey(username);
  }

  private String userIdKey(@NonNull Long userId) {
    return authCacheKeyConfig.refreshUserIdKey(userId);
  }
}
