package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.calculator.AuthorityHashCalculator;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import com.google.common.base.MoreObjects;
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
  private final AuthorityHashCalculator hashCalculator;
  private final UserAuthorityRedisStore redisStore;
  private final UserAuthorityRefCounter refCounter;

  public UserAuthorityStore(
      @NonNull AuthorityHashCalculator hashCalculator,
      @NonNull UserAuthorityRedisStore redisStore,
      @NonNull UserAuthorityRefCounter refCounter) {
    this.hashCalculator = hashCalculator;
    this.redisStore = redisStore;
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

    redisStore.get(userId).ifPresent(this::releaseSharedAuthorities);

    refCounter.adoptRoleSet(roleHash, roles);
    refCounter.adoptPermissionSet(permissionHash, permissions);

    redisStore.save(
        UserAuthorityRecordBuilder.builder()
            .userId(userId)
            .username(username)
            .displayName(displayName)
            .userType(userType)
            .authorityVersion(snapshot.authorityVersion())
            .roleHash(roleHash)
            .permissionHash(permissionHash)
            .roleCodes(roles)
            .permissionCodes(permissions)
            .build(),
        authorityHash);
  }

  public Optional<PermissionSnapshot> get(@NonNull Long userId) {
    return redisStore.get(userId).map(this::toSnapshot);
  }

  public Optional<Long> resolveUserId(@NonNull String username) {
    return redisStore.resolveUserId(username);
  }

  public void delete(@NonNull Long userId) {
    redisStore.delete(userId).ifPresent(this::releaseSharedAuthorities);
  }

  private PermissionSnapshot toSnapshot(@NonNull UserAuthorityRecord record) {
    val roleCodes = MoreObjects.firstNonNull(record.roleCodes(), Set.<String>of());
    val permissionCodes = MoreObjects.firstNonNull(record.permissionCodes(), Set.<String>of());
    val versionForReuse = StringUtils.defaultString(record.authorityVersion());
    val attributes = new LinkedHashMap<String, Object>();
    attributes.put(
        PrincipalAttributeKeys.DISPLAY_NAME,
        MoreObjects.firstNonNull(record.displayName(), record.username()));
    attributes.put(
        PrincipalAttributeKeys.USER_TYPE, MoreObjects.firstNonNull(record.userType(), "ADMIN"));
    attributes.put(PrincipalAttributeKeys.ROLES, roleCodes);
    attributes.put(PrincipalAttributeKeys.PERMISSIONS, permissionCodes);
    attributes.put(PrincipalAttributeKeys.AUTHORITY_VERSION, versionForReuse);
    return new PermissionSnapshot(
        record.username(),
        MoreObjects.firstNonNull(record.displayName(), record.username()),
        MoreObjects.firstNonNull(record.userType(), "ADMIN"),
        roleCodes,
        permissionCodes,
        versionForReuse,
        attributes,
        record.userId());
  }

  private void releaseSharedAuthorities(@NonNull UserAuthorityRecord record) {
    refCounter.releaseRoleSet(record.roleHash());
    refCounter.releasePermissionSet(record.permissionHash());
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
