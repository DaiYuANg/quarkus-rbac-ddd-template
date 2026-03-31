package com.github.DaiYuANg.cache;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.val;

@ApplicationScoped
public class RefreshTokenStore {
  public record RefreshTokenOwner(Long userId, String username) {}

  private final RefreshTokenRedisStore redisStore;

  public RefreshTokenStore(@NonNull RefreshTokenRedisStore redisStore) {
    this.redisStore = redisStore;
  }

  public void save(@NonNull String refreshToken, Long userId, String username, @NonNull Duration ttl) {
    val normalizedUsername = RefreshTokenOwnerCodec.normalize(username);
    redisStore.save(
        refreshToken,
        RefreshTokenOwnerCodec.encode(userId, normalizedUsername),
        userId,
        normalizedUsername,
        ttl);
  }

  public Optional<String> getUsername(@NonNull String refreshToken) {
    return getOwner(refreshToken)
        .map(RefreshTokenOwner::username)
        .map(RefreshTokenOwnerCodec::normalize);
  }

  public Optional<RefreshTokenOwner> getOwner(@NonNull String refreshToken) {
    return RefreshTokenOwnerCodec.decode(redisStore.getOwnerPayload(refreshToken));
  }

  public void delete(@NonNull String refreshToken) {
    val owner = getOwner(refreshToken).orElse(null);
    redisStore.deleteToken(refreshToken);
    redisStore.cleanupMembership(owner, refreshToken);
  }

  public void deleteByUsername(String username) {
    val normalizedUsername = RefreshTokenOwnerCodec.normalize(username);
    if (normalizedUsername == null) {
      return;
    }
    redisStore.findTokensByUsername(normalizedUsername).stream()
        .map(RefreshTokenOwnerCodec::normalize)
        .filter(Objects::nonNull)
        .forEach(this::delete);
    redisStore.deleteUsernameIndex(normalizedUsername);
  }

  public void deleteByUserId(Long userId) {
    if (userId == null) {
      return;
    }
    redisStore.findTokensByUserId(userId).stream()
        .map(RefreshTokenOwnerCodec::normalize)
        .filter(Objects::nonNull)
        .forEach(this::delete);
    redisStore.deleteUserIdIndex(userId);
  }
}
