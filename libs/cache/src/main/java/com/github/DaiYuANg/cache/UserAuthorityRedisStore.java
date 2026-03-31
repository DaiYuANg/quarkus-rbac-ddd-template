package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.config.RBACCacheProperties;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import com.google.common.base.MoreObjects;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
class UserAuthorityRedisStore {
  private final ValueCommands<String, String> valueCommands;
  private final HashCommands<String, String, String> hashCommands;
  private final SetCommands<String, String> setCommands;
  private final KeyCommands<String> keyCommands;
  private final RBACCacheProperties props;

  UserAuthorityRedisStore(@NonNull RedisDataSource ds, @NonNull RBACCacheProperties props) {
    this.valueCommands = ds.value(String.class);
    this.hashCommands = ds.hash(String.class);
    this.setCommands = ds.set(String.class);
    this.keyCommands = ds.key();
    this.props = props;
  }

  Optional<UserAuthorityRecord> get(@NonNull Long userId) {
    val authorityKey = props.authorityKey(userId);
    if (!keyCommands.exists(authorityKey)) {
      return Optional.empty();
    }

    val authorityHashKey = props.authorityHashKey(userId);
    val roleHash = normalize(hashCommands.hget(authorityHashKey, props.authorityHashRefRoleKey()));
    val permissionHash =
        normalize(hashCommands.hget(authorityHashKey, props.authorityHashRefPermissionKey()));
    val username = hashCommands.hget(authorityHashKey, PrincipalAttributeKeys.USERNAME);
    if (username == null) {
      return Optional.empty();
    }

    return Optional.of(
        UserAuthorityRecordBuilder.builder()
            .userId(userId)
            .username(username)
            .displayName(
                normalize(hashCommands.hget(authorityHashKey, PrincipalAttributeKeys.DISPLAY_NAME)))
            .userType(normalize(hashCommands.hget(authorityHashKey, PrincipalAttributeKeys.USER_TYPE)))
            .authorityVersion(
                hashCommands.hget(authorityHashKey, PrincipalAttributeKeys.AUTHORITY_VERSION))
            .roleHash(roleHash)
            .permissionHash(permissionHash)
            .roleCodes(loadCodes(roleHash, props::roleHashKey))
            .permissionCodes(loadCodes(permissionHash, props::permissionHashKey))
            .build());
  }

  void save(@NonNull UserAuthorityRecord record, @NonNull String authorityHash) {
    val authorityHashKey = props.authorityHashKey(record.userId());
    hashCommands.hset(authorityHashKey, props.authorityHashRefRoleKey(), record.roleHash());
    hashCommands.hset(
        authorityHashKey, props.authorityHashRefPermissionKey(), record.permissionHash());
    hashCommands.hset(authorityHashKey, PrincipalAttributeKeys.USERNAME, record.username());
    hashCommands.hset(
        authorityHashKey, PrincipalAttributeKeys.DISPLAY_NAME, record.displayName());
    hashCommands.hset(authorityHashKey, PrincipalAttributeKeys.USER_TYPE, record.userType());
    hashCommands.hset(
        authorityHashKey, PrincipalAttributeKeys.AUTHORITY_VERSION, record.authorityVersion());

    valueCommands.set(props.authorityKey(record.userId()), authorityHash);
    valueCommands.set(props.usernameToUserIdKey(record.username()), String.valueOf(record.userId()));
  }

  Optional<Long> resolveUserId(@NonNull String username) {
    val raw = normalize(valueCommands.get(props.usernameToUserIdKey(username)));
    if (raw == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(Long.parseLong(raw));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  Optional<UserAuthorityRecord> delete(@NonNull Long userId) {
    val existing = get(userId);
    val authorityHashKey = props.authorityHashKey(userId);
    keyCommands.del(props.authorityKey(userId));
    keyCommands.del(authorityHashKey);
    existing.map(UserAuthorityRecord::username).ifPresent(username -> keyCommands.del(props.usernameToUserIdKey(username)));
    return existing;
  }

  private Set<String> loadCodes(
      String hash, @NonNull java.util.function.Function<String, String> keyFactory) {
    if (hash == null) {
      return Set.of();
    }
    return MoreObjects.firstNonNull(setCommands.smembers(keyFactory.apply(hash)), Set.<String>of());
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
