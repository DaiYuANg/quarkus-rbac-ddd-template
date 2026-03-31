package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.calculator.AuthorityHashCalculator;
import com.github.DaiYuANg.cache.config.RBACCacheProperties;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import com.google.common.base.MoreObjects;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

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
 *
 * <p>Design notes:
 *
 * <ul>
 *   <li>{@code userId} is the primary key for snapshots. DB users use row ids; virtual principals
 *       such as the configured super admin use stable synthetic negative ids.
 *   <li>Role/permission sets are stored by hash and ref-counted to reduce memory footprint.
 *   <li>{@code authorityVersion} is persisted to support fast reuse / invalidation.
 * </ul>
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
public class UserAuthorityStore {

  private final ValueCommands<String, String> valueCommands;
  private final HashCommands<String, String, String> hashCommands;
  private final SetCommands<String, String> setCommands;
  private final KeyCommands<String> keyCommands;
  private final AuthorityHashCalculator hashCalculator;
  private final RBACCacheProperties props;
  private final UserAuthorityRefCounter refCounter;

  public UserAuthorityStore(
      @NonNull RedisDataSource ds,
      @NonNull AuthorityHashCalculator hashCalculator,
      @NonNull RBACCacheProperties props,
      @NonNull UserAuthorityRefCounter refCounter) {
    this.valueCommands = ds.value(String.class);
    this.hashCommands = ds.hash(String.class);
    this.setCommands = ds.set(String.class);
    this.keyCommands = ds.key();
    this.hashCalculator = hashCalculator;
    this.props = props;
    this.refCounter = refCounter;
  }

  /** Save user authority. Uses refcounting for shared role/permission sets. */
  public void save(@NonNull Long userId, @NonNull PermissionSnapshot snapshot) {
    val username = snapshot.username();
    val roles = MoreObjects.firstNonNull(snapshot.roles(), Set.<String>of());
    val permissions = MoreObjects.firstNonNull(snapshot.permissions(), Set.<String>of());
    val displayName = MoreObjects.firstNonNull(normalize(snapshot.displayName()), username);
    val userType = MoreObjects.firstNonNull(normalize(snapshot.userType()), "ADMIN");

    val roleHash = hashCalculator.generateRoleHash(roles);
    val permissionHash = hashCalculator.generatePermissionHash(permissions);
    val authorityHash = hashCalculator.generateAuthorityKey(roles, permissions);

    val authorityHashKey = props.authorityHashKey(userId);

    val existingRoleHash = hashCommands.hget(authorityHashKey, props.authorityHashRefRoleKey());
    val existingPermissionHash =
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
    hashCommands.hset(authorityHashKey, PrincipalAttributeKeys.USERNAME, username);
    hashCommands.hset(authorityHashKey, PrincipalAttributeKeys.DISPLAY_NAME, displayName);
    hashCommands.hset(authorityHashKey, PrincipalAttributeKeys.USER_TYPE, userType);
    hashCommands.hset(
        authorityHashKey, PrincipalAttributeKeys.AUTHORITY_VERSION, snapshot.authorityVersion());

    valueCommands.set(props.authorityKey(userId), authorityHash);
    valueCommands.set(props.usernameToUserIdKey(username), String.valueOf(userId));
  }

  public Optional<PermissionSnapshot> get(@NonNull Long userId) {
    val authorityKey = props.authorityKey(userId);
    if (!keyCommands.exists(authorityKey)) {
      return Optional.empty();
    }

    val authorityHashKey = props.authorityHashKey(userId);
    val roleHash = normalize(hashCommands.hget(authorityHashKey, props.authorityHashRefRoleKey()));
    val permissionHash =
        normalize(hashCommands.hget(authorityHashKey, props.authorityHashRefPermissionKey()));
    val username = hashCommands.hget(authorityHashKey, PrincipalAttributeKeys.USERNAME);
    val displayName = normalize(hashCommands.hget(authorityHashKey, PrincipalAttributeKeys.DISPLAY_NAME));
    val userType = normalize(hashCommands.hget(authorityHashKey, PrincipalAttributeKeys.USER_TYPE));
    val authorityVersion =
        hashCommands.hget(authorityHashKey, PrincipalAttributeKeys.AUTHORITY_VERSION);

    if (username == null) {
      return Optional.empty();
    }

    val roleCodes =
        roleHash == null
            ? Set.<String>of()
            : MoreObjects.firstNonNull(
                setCommands.smembers(props.roleHashKey(roleHash)), Set.<String>of());
    val permissionCodes =
        permissionHash == null
            ? Set.<String>of()
            : MoreObjects.firstNonNull(
                setCommands.smembers(props.permissionHashKey(permissionHash)), Set.<String>of());

    val versionForReuse = StringUtils.defaultString(authorityVersion);
    val attributes = new LinkedHashMap<String, Object>();
    attributes.put(
        PrincipalAttributeKeys.DISPLAY_NAME, MoreObjects.firstNonNull(displayName, username));
    attributes.put(PrincipalAttributeKeys.USER_TYPE, MoreObjects.firstNonNull(userType, "ADMIN"));
    attributes.put(PrincipalAttributeKeys.ROLES, roleCodes);
    attributes.put(PrincipalAttributeKeys.PERMISSIONS, permissionCodes);
    attributes.put(PrincipalAttributeKeys.AUTHORITY_VERSION, versionForReuse);

    return Optional.of(
        new PermissionSnapshot(
            username,
            MoreObjects.firstNonNull(displayName, username),
            MoreObjects.firstNonNull(userType, "ADMIN"),
            roleCodes,
            permissionCodes,
            versionForReuse,
            attributes,
            userId));
  }

  public Optional<Long> resolveUserId(@NonNull String username) {
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

  public void delete(@NonNull Long userId) {
    val authorityHashKey = props.authorityHashKey(userId);
    val roleHash = hashCommands.hget(authorityHashKey, props.authorityHashRefRoleKey());
    val permissionHash = hashCommands.hget(authorityHashKey, props.authorityHashRefPermissionKey());
    val username = normalize(hashCommands.hget(authorityHashKey, PrincipalAttributeKeys.USERNAME));

    keyCommands.del(props.authorityKey(userId));
    keyCommands.del(authorityHashKey);
    if (username != null) {
      keyCommands.del(props.usernameToUserIdKey(username));
    }

    refCounter.releaseRoleSet(roleHash);
    refCounter.releasePermissionSet(permissionHash);
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
