package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.config.AuthCacheKeyConfig;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * One-shot nonce storage for HTTP replay protection. Uses the default Quarkus {@link
 * RedisDataSource} (same {@code quarkus.redis.*} config as the rest of the app); no extra Redis
 * client.
 */
@ApplicationScoped
public class ReplayNonceStore {

  private final ValueCommands<String, String> valueCommands;
  private final KeyCommands<String> keyCommands;
  private final AuthCacheKeyConfig authCacheKeyConfig;

  public ReplayNonceStore(
      @NonNull RedisDataSource redis, @NonNull AuthCacheKeyConfig authCacheKeyConfig) {
    this.valueCommands = redis.value(String.class);
    this.keyCommands = redis.key();
    this.authCacheKeyConfig = authCacheKeyConfig;
  }

  /**
   * @return {@code true} if this nonce was accepted (first use), {@code false} if already seen or
   *     Redis rejected the write
   */
  public boolean tryConsumeOnce(String nonce, @NonNull Duration ttl) {
    val normalizedNonce = normalize(nonce);
    if (normalizedNonce == null) {
      return false;
    }
    val seconds = (int) Math.clamp(ttl.toSeconds(), 1, Integer.MAX_VALUE);
    val key = authCacheKeyConfig.replayNonceKey(sha256Hex(normalizedNonce));
    if (!valueCommands.setnx(key, "1")) {
      return false;
    }
    keyCommands.expire(key, seconds);
    return true;
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }

  private static String sha256Hex(String raw) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
