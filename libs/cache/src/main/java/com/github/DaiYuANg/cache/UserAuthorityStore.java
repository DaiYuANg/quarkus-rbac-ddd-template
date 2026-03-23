package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.calculator.AuthorityHashCalculator;
import com.github.DaiYuANg.cache.config.RBACCacheProperties;
import com.github.DaiYuANg.security.PermissionSnapshot;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * User authority store with hash-based deduplication and reference counting.
 * <p>
 * Structure:
 * <ul>
 *   <li>authorityKey(userId) -> authorityHash (user's role+permission fingerprint)</li>
 *   <li>authorityHashKey(userId) -> Hash{roleHash, permissionHash, username, displayName, userType}</li>
 *   <li>roleHashKey(roleHash) -> Set of roleCodes (shared, refcounted)</li>
 *   <li>permissionHashKey(permissionHash) -> Set of permissionCodes (shared, refcounted)</li>
 *   <li>usernameToUserIdKey(username) -> userId (for lookup by username)</li>
 *   <li>roleRefCountKey(roleHash) / permissionRefCountKey(permissionHash) -> refcount</li>
 * </ul>
 */
@ApplicationScoped
public class UserAuthorityStore {

    private static final String HASH_FIELD_USERNAME = "username";
    private static final String HASH_FIELD_DISPLAY_NAME = "displayName";
    private static final String HASH_FIELD_USER_TYPE = "userType";
    private static final String HASH_FIELD_AUTHORITY_VERSION = "authorityVersion";

    private final ValueCommands<String, String> valueCommands;
    private final ValueCommands<String, Long> refCountCommands;
    private final HashCommands<String, String, String> hashCommands;
    private final SetCommands<String, String> setCommands;
    private final KeyCommands<String> keyCommands;
    private final AuthorityHashCalculator hashCalculator;
    private final RBACCacheProperties props;

    public UserAuthorityStore(
            RedisDataSource ds,
            AuthorityHashCalculator hashCalculator,
            RBACCacheProperties props) {
        this.valueCommands = ds.value(String.class);
        this.refCountCommands = ds.value(Long.class);
        this.hashCommands = ds.hash(String.class);
        this.setCommands = ds.set(String.class);
        this.keyCommands = ds.key();
        this.hashCalculator = hashCalculator;
        this.props = props;
    }

    /**
     * Save user authority. Uses refcounting for shared role/permission sets.
     */
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

        // Decrement refcount for previous hashes if user had prior data
        var existingRoleHash = hashCommands.hget(authorityHashKey, props.authorityHashRefRoleKey());
        var existingPermissionHash = hashCommands.hget(authorityHashKey, props.authorityHashRefPermissionKey());
        if (existingRoleHash != null) {
            decrementRefAndMaybeDeleteRole(existingRoleHash);
        }
        if (existingPermissionHash != null) {
            decrementRefAndMaybeDeletePermission(existingPermissionHash);
        }

        // Create or add ref to role set
        var roleHashKey = props.roleHashKey(roleHash);
        if (!roles.isEmpty()) {
            if (!keyCommands.exists(roleHashKey)) {
                setCommands.sadd(roleHashKey, roles.toArray(new String[0]));
                refCountCommands.set(props.roleRefCountKey(roleHash), 1L);
            } else {
                refCountCommands.incr(props.roleRefCountKey(roleHash));
            }
        }

        // Create or add ref to permission set
        var permissionHashKey = props.permissionHashKey(permissionHash);
        if (!permissions.isEmpty()) {
            if (!keyCommands.exists(permissionHashKey)) {
                setCommands.sadd(permissionHashKey, permissions.toArray(new String[0]));
                refCountCommands.set(props.permissionRefCountKey(permissionHash), 1L);
            } else {
                refCountCommands.incr(props.permissionRefCountKey(permissionHash));
            }
        }

        // Store user hash mapping
        hashCommands.hset(authorityHashKey, props.authorityHashRefRoleKey(), roleHash);
        hashCommands.hset(authorityHashKey, props.authorityHashRefPermissionKey(), permissionHash);
        hashCommands.hset(authorityHashKey, HASH_FIELD_USERNAME, username);
        hashCommands.hset(authorityHashKey, HASH_FIELD_DISPLAY_NAME, displayName);
        hashCommands.hset(authorityHashKey, HASH_FIELD_USER_TYPE, userType);
        hashCommands.hset(authorityHashKey, HASH_FIELD_AUTHORITY_VERSION, snapshot.authorityVersion());

        // Store authority fingerprint (for internal use)
        valueCommands.set(props.authorityKey(userId), authorityHash);

        // Username -> userId mapping for lookup
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

        var roleCodes = roleHash != null && !roleHash.isBlank()
            ? setCommands.smembers(props.roleHashKey(roleHash))
            : Set.<String>of();
        var permissionCodes = permissionHash != null && !permissionHash.isBlank()
            ? setCommands.smembers(props.permissionHashKey(permissionHash))
            : Set.<String>of();

        var versionForReuse = authorityVersion != null ? authorityVersion : "";
        var attributes = new LinkedHashMap<String, Object>();
        attributes.put("displayName", displayName != null ? displayName : username);
        attributes.put("userType", userType != null ? userType : "ADMIN");
        attributes.put("roles", roleCodes);
        attributes.put("permissions", permissionCodes);
        attributes.put("authorityVersion", versionForReuse);

        return Optional.of(new PermissionSnapshot(
            username,
            displayName != null ? displayName : username,
            userType != null ? userType : "ADMIN",
            roleCodes,
            permissionCodes,
            versionForReuse,
            attributes,
            userId
        ));
    }

    /**
     * Lookup userId by username.
     */
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

    /**
     * Delete user authority and decrement refcounts. Shared sets are removed only when refcount reaches 0.
     */
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

        if (roleHash != null && !roleHash.isBlank()) {
            decrementRefAndMaybeDeleteRole(roleHash);
        }
        if (permissionHash != null && !permissionHash.isBlank()) {
            decrementRefAndMaybeDeletePermission(permissionHash);
        }
    }

    private void decrementRefAndMaybeDeleteRole(String roleHash) {
        var roleHashKey = props.roleHashKey(roleHash);
        if (!keyCommands.exists(roleHashKey)) {
            return; // Set was never created (empty roles), no refcount to decrement
        }
        var refKey = props.roleRefCountKey(roleHash);
        var count = refCountCommands.decr(refKey);
        if (count <= 0) {
            keyCommands.del(roleHashKey);
            keyCommands.del(refKey);
        }
    }

    private void decrementRefAndMaybeDeletePermission(String permissionHash) {
        var permissionHashKey = props.permissionHashKey(permissionHash);
        if (!keyCommands.exists(permissionHashKey)) {
            return; // Set was never created (empty permissions), no refcount to decrement
        }
        var refKey = props.permissionRefCountKey(permissionHash);
        var count = refCountCommands.decr(refKey);
        if (count <= 0) {
            keyCommands.del(permissionHashKey);
            keyCommands.del(refKey);
        }
    }
}
