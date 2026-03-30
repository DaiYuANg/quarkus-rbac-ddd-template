package com.github.DaiYuANg.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

/**
 * One-shot nonce storage for HTTP replay protection. Uses the default Quarkus {@link
 * RedisDataSource} (same {@code quarkus.redis.*} config as the rest of the app); no extra Redis
 * client.
 */
@ApplicationScoped
public class ReplayNonceStore {

  private static final String KEY_PREFIX = "replay:nonce:";

  private final ValueCommands<String, String> valueCommands;
  private final KeyCommands<String> keyCommands;

  public ReplayNonceStore(RedisDataSource redis) {
    this.valueCommands = redis.value(String.class);
    this.keyCommands = redis.key();
  }

  /**
   * @return {@code true} if this nonce was accepted (first use), {@code false} if already seen or
   *     Redis rejected the write
   */
  public boolean tryConsumeOnce(String nonce, Duration ttl) {
    if (nonce == null || nonce.isBlank()) {
      return false;
    }
    int seconds = (int) Math.clamp(ttl.toSeconds(), 1, Integer.MAX_VALUE);
    String key = KEY_PREFIX + sha256Hex(nonce);
    if (!valueCommands.setnx(key, "1")) {
      return false;
    }
    keyCommands.expire(key, seconds);
    return true;
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
