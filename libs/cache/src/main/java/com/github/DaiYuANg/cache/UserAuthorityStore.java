package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.calculator.AuthorityHashCalculator;
import com.github.DaiYuANg.cache.config.RBACCacheProperties;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;

/**
 * User authority store with hash-based deduplication and reference counting.
 *
 * <p>Structure:
 *
 * <ul>
 *   <li>authorityKey(userId) -> authorityHash (user's role+permission fingerprint)
 *   <li>authorityHashKey(userId) -> Hash{roleHash, permissionHash, username, displayName, userType}
 *   <li>roleHashKey(roleHash) -> Set of roleCodes (shared, refcounted)
 *   <li>permissionHashKey(permissionHash) -> Set of permissionCodes (shared, refcounted)
 *   <li>usernameToUserIdKey(username) -> userId (for lookup by username)
 *   <li>roleRefCountKey(roleHash) / permissionRefCountKey(permissionHash) -> refcount
 * </ul>
 */
@ApplicationScoped
public class UserAuthorityStore {

  private static final String HASH_FIELD_USERNAME = "username";
  private static final String HASH_FIELD_DISPLAY_NAME = "displayName";
  private static final String HASH_FIELD_USER_TYPE = "userType";
  private static final String HASH_FIELD_AUTHORITY_VERSION = "authorityVersion";

  private final ValueCommands<String, String> valueCommands;
  private final HashCommands<String, String, String> hashCommands;
  private final SetCommands<String, String> setCommands;
  private final KeyCommands<String> keyCommands;
  private final AuthorityHashCalculator hashCalculator;
  private final RBACCacheProperties props;
  private final UserAuthorityRefCounter refCounter;

  public UserAuthorityStore(
      RedisDataSource ds,
      AuthorityHashCalculator hashCalculator,
      RBACCacheProperties props,
      UserAuthorityRefCounter refCounter) {
    this.valueCommands = ds.value(String.class);
    this.hashCommands = ds.hash(String.class);
    this.setCommands = ds.set(String.class);
    this.keyCommands = ds.key();
    this.hashCalculator = hashCalculator;
    this.props = props;
    this.refCounter = refCounter;
  }

  /** Save user authority. Uses refcounting for shared role/permission sets. */
  public void save(Long userId, PermissionSnapshot snapshot) {
    var username = snapshot.username();
    var roles = snapshot.roles() == null ? Set.<String>of() : snapshot.roles();
    var permissions = snapshot.permissions() == null ? Set.<String>of() : snapshot.permissions();
    var displayName = snapshot.displayName() == null ? username : snapshot.displayName();
    var userType = snapshot.userType() == null ? "ADMIN" : snapshot.userType();

    var roleHash = hashCalculator.generateRoleHash(roles);
    var permissionHash = hashCalculator.generatePermissionHash(permissions);
    var authorityHash = hashCalculator.generateAuthorityKey(roles, permissions);

    var authorityHashKey = props.authorityHashKey(userId);

    var existingRoleHash = hashCommands.hget(authorityHashKey, props.authorityHashRefRoleKey());
    var existingPermissionHash =
        hashCommands.hget(authorityHashKey, props.authorityHashRefPermissionKey());
    if (existingRoleHash != null) {
      refCounter.releaseRoleSet(existingRoleHash);
    }
    if (existingPermissionHash != null) {
      refCounter.releasePermissionSet(existingPermissionHash);
    }

    refCounter.adoptRoleSet(roleHash, roles);
    refCounter.adoptPermissionSet(permissionHash, permissions);

    hashCommands.hset(authorityHashKey, props.authorityHashRefRoleKey(), roleHash);
    hashCommands.hset(authorityHashKey, props.authorityHashRefPermissionKey(), permissionHash);
    hashCommands.hset(authorityHashKey, HASH_FIELD_USERNAME, username);
    hashCommands.hset(authorityHashKey, HASH_FIELD_DISPLAY_NAME, displayName);
    hashCommands.hset(authorityHashKey, HASH_FIELD_USER_TYPE, userType);
    hashCommands.hset(authorityHashKey, HASH_FIELD_AUTHORITY_VERSION, snapshot.authorityVersion());

    valueCommands.set(props.authorityKey(userId), authorityHash);
    valueCommands.set(props.usernameToUserIdKey(username), String.valueOf(userId));
  }

  public Optional<PermissionSnapshot> get(Long userId) {
    var authorityKey = props.authorityKey(userId);
    if (!keyCommands.exists(authorityKey)) {
      return Optional.empty();
    }

    var authorityHashKey = props.authorityHashKey(userId);
    var roleHash = hashCommands.hget(authorityHashKey, props.authorityHashRefRoleKey());
    var permissionHash = hashCommands.hget(authorityHashKey, props.authorityHashRefPermissionKey());
    var username = hashCommands.hget(authorityHashKey, HASH_FIELD_USERNAME);
    var displayName = hashCommands.hget(authorityHashKey, HASH_FIELD_DISPLAY_NAME);
    var userType = hashCommands.hget(authorityHashKey, HASH_FIELD_USER_TYPE);
    var authorityVersion = hashCommands.hget(authorityHashKey, HASH_FIELD_AUTHORITY_VERSION);

    if (username == null) {
      return Optional.empty();
    }

    var roleCodes =
        roleHash != null && !roleHash.isBlank()
            ? setCommands.smembers(props.roleHashKey(roleHash))
            : Set.<String>of();
    var permissionCodes =
        permissionHash != null && !permissionHash.isBlank()
            ? setCommands.smembers(props.permissionHashKey(permissionHash))
            : Set.<String>of();

    var versionForReuse = authorityVersion != null ? authorityVersion : "";
    var attributes = new LinkedHashMap<String, Object>();
    attributes.put("displayName", displayName != null ? displayName : username);
    attributes.put("userType", userType != null ? userType : "ADMIN");
    attributes.put("roles", roleCodes);
    attributes.put("permissions", permissionCodes);
    attributes.put("authorityVersion", versionForReuse);

    return Optional.of(
        new PermissionSnapshot(
            username,
            displayName != null ? displayName : username,
            userType != null ? userType : "ADMIN",
            roleCodes,
            permissionCodes,
            versionForReuse,
            attributes,
            userId));
  }

  public Optional<Long> resolveUserId(String username) {
    var raw = valueCommands.get(props.usernameToUserIdKey(username));
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    try {
      return Optional.of(Long.parseLong(raw.trim()));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  public void delete(Long userId) {
    var authorityHashKey = props.authorityHashKey(userId);
    var roleHash = hashCommands.hget(authorityHashKey, props.authorityHashRefRoleKey());
    var permissionHash = hashCommands.hget(authorityHashKey, props.authorityHashRefPermissionKey());
    var username = hashCommands.hget(authorityHashKey, HASH_FIELD_USERNAME);

    keyCommands.del(props.authorityKey(userId));
    keyCommands.del(authorityHashKey);
    if (username != null && !username.isBlank()) {
      keyCommands.del(props.usernameToUserIdKey(username));
    }

    refCounter.releaseRoleSet(roleHash);
    refCounter.releasePermissionSet(permissionHash);
  }
}
