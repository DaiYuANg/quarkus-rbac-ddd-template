package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.calculator.AuthorityHashCalculator;
import com.github.DaiYuANg.cache.config.RBACCacheProperties;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.hash.ReactiveHashCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.set.ReactiveSetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
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
    private final ReactiveValueCommands<String, String> reactiveValueCommands;
    private final ReactiveValueCommands<String, Long> reactiveRefCountCommands;
    private final ReactiveHashCommands<String, String, String> reactiveHashCommands;
    private final ReactiveSetCommands<String, String> reactiveSetCommands;
    private final ReactiveKeyCommands<String> reactiveKeyCommands;
    private final AuthorityHashCalculator hashCalculator;
    private final RBACCacheProperties props;

    public UserAuthorityStore(
            RedisDataSource ds,
            ReactiveRedisDataSource reactiveDs,
            AuthorityHashCalculator hashCalculator,
            RBACCacheProperties props) {
        this.valueCommands = ds.value(String.class);
        this.refCountCommands = ds.value(Long.class);
        this.hashCommands = ds.hash(String.class);
        this.setCommands = ds.set(String.class);
        this.keyCommands = ds.key();
        this.reactiveValueCommands = reactiveDs.value(String.class);
        this.reactiveRefCountCommands = reactiveDs.value(Long.class);
        this.reactiveHashCommands = reactiveDs.hash(String.class);
        this.reactiveSetCommands = reactiveDs.set(String.class);
        this.reactiveKeyCommands = reactiveDs.key();
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

    public Uni<Optional<Long>> resolveUserIdAsync(String username) {
        return reactiveValueCommands.get(props.usernameToUserIdKey(username))
            .onItem().transform(raw -> {
                if (raw == null || raw.isBlank()) {
                    return Optional.<Long>empty();
                }
                try {
                    return Optional.of(Long.parseLong(raw.trim()));
                } catch (NumberFormatException e) {
                    return Optional.<Long>empty();
                }
            });
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

    public Uni<Void> saveAsync(Long userId, PermissionSnapshot snapshot) {
        var username = snapshot.username();
        var roles = snapshot.roles() == null ? Set.<String>of() : snapshot.roles();
        var permissions = snapshot.permissions() == null ? Set.<String>of() : snapshot.permissions();
        var displayName = snapshot.displayName() == null ? username : snapshot.displayName();
        var userType = snapshot.userType() == null ? "ADMIN" : snapshot.userType();

        var roleHash = hashCalculator.generateRoleHash(roles);
        var permissionHash = hashCalculator.generatePermissionHash(permissions);
        var authorityHash = hashCalculator.generateAuthorityKey(roles, permissions);
        var authorityHashKey = props.authorityHashKey(userId);

        Uni<String> existingRoleHashUni = reactiveHashCommands.hget(authorityHashKey, props.authorityHashRefRoleKey());
        Uni<String> existingPermissionHashUni = reactiveHashCommands.hget(authorityHashKey, props.authorityHashRefPermissionKey());

        return Uni.combine().all().unis(existingRoleHashUni, existingPermissionHashUni).asTuple()
            .chain(tuple -> {
                var chain = Uni.createFrom().voidItem();
                String existingRoleHash = tuple.getItem1();
                String existingPermissionHash = tuple.getItem2();
                if (existingRoleHash != null) {
                    chain = chain.chain(() -> decrementRefAndMaybeDeleteRoleAsync(existingRoleHash));
                }
                if (existingPermissionHash != null) {
                    chain = chain.chain(() -> decrementRefAndMaybeDeletePermissionAsync(existingPermissionHash));
                }
                return chain;
            })
            .chain(() -> upsertRoleSetAsync(roleHash, roles))
            .chain(() -> upsertPermissionSetAsync(permissionHash, permissions))
            .chain(() -> reactiveHashCommands.hset(authorityHashKey, props.authorityHashRefRoleKey(), roleHash))
            .chain(() -> reactiveHashCommands.hset(authorityHashKey, props.authorityHashRefPermissionKey(), permissionHash))
            .chain(() -> reactiveHashCommands.hset(authorityHashKey, HASH_FIELD_USERNAME, username))
            .chain(() -> reactiveHashCommands.hset(authorityHashKey, HASH_FIELD_DISPLAY_NAME, displayName))
            .chain(() -> reactiveHashCommands.hset(authorityHashKey, HASH_FIELD_USER_TYPE, userType))
            .chain(() -> reactiveHashCommands.hset(authorityHashKey, HASH_FIELD_AUTHORITY_VERSION, snapshot.authorityVersion()))
            .chain(() -> reactiveValueCommands.set(props.authorityKey(userId), authorityHash))
            .chain(() -> reactiveValueCommands.set(props.usernameToUserIdKey(username), String.valueOf(userId)))
            .replaceWithVoid();
    }

    public Uni<Optional<PermissionSnapshot>> getAsync(Long userId) {
        var authorityKey = props.authorityKey(userId);
        return reactiveKeyCommands.exists(authorityKey).chain(exists -> {
            if (!exists) {
                return Uni.createFrom().item(Optional.<PermissionSnapshot>empty());
            }
            var authorityHashKey = props.authorityHashKey(userId);
            Uni<String> roleHashUni = reactiveHashCommands.hget(authorityHashKey, props.authorityHashRefRoleKey());
            Uni<String> permissionHashUni = reactiveHashCommands.hget(authorityHashKey, props.authorityHashRefPermissionKey());
            Uni<String> usernameUni = reactiveHashCommands.hget(authorityHashKey, HASH_FIELD_USERNAME);
            Uni<String> displayNameUni = reactiveHashCommands.hget(authorityHashKey, HASH_FIELD_DISPLAY_NAME);
            Uni<String> userTypeUni = reactiveHashCommands.hget(authorityHashKey, HASH_FIELD_USER_TYPE);
            Uni<String> authorityVersionUni = reactiveHashCommands.hget(authorityHashKey, HASH_FIELD_AUTHORITY_VERSION);

            return Uni.combine().all().unis(
                roleHashUni,
                permissionHashUni,
                usernameUni,
                displayNameUni,
                userTypeUni,
                authorityVersionUni
            ).asTuple().chain(tuple -> {
                String roleHash = tuple.getItem1();
                String permissionHash = tuple.getItem2();
                String username = tuple.getItem3();
                String displayName = tuple.getItem4();
                String userType = tuple.getItem5();
                String authorityVersion = tuple.getItem6();

                if (username == null) {
                    return Uni.createFrom().item(Optional.<PermissionSnapshot>empty());
                }

                Uni<Set<String>> roleCodesUni = (roleHash != null && !roleHash.isBlank())
                    ? reactiveSetCommands.smembers(props.roleHashKey(roleHash))
                    : Uni.createFrom().item(Set.<String>of());
                Uni<Set<String>> permissionCodesUni = (permissionHash != null && !permissionHash.isBlank())
                    ? reactiveSetCommands.smembers(props.permissionHashKey(permissionHash))
                    : Uni.createFrom().item(Set.<String>of());

                return Uni.combine().all().unis(roleCodesUni, permissionCodesUni).asTuple()
                    .onItem().transform(codes -> {
                        Set<String> roleCodes = codes.getItem1();
                        Set<String> permissionCodes = codes.getItem2();
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
                    });
            });
        });
    }

    public Uni<Void> deleteAsync(Long userId) {
        var authorityHashKey = props.authorityHashKey(userId);
        Uni<String> roleHashUni = reactiveHashCommands.hget(authorityHashKey, props.authorityHashRefRoleKey());
        Uni<String> permissionHashUni = reactiveHashCommands.hget(authorityHashKey, props.authorityHashRefPermissionKey());
        Uni<String> usernameUni = reactiveHashCommands.hget(authorityHashKey, HASH_FIELD_USERNAME);
        return Uni.combine().all().unis(roleHashUni, permissionHashUni, usernameUni).asTuple()
            .chain(tuple -> {
                String roleHash = tuple.getItem1();
                String permissionHash = tuple.getItem2();
                String username = tuple.getItem3();
                var chain = Uni.createFrom().voidItem()
                    .chain(() -> reactiveKeyCommands.del(props.authorityKey(userId)).replaceWithVoid())
                    .chain(() -> reactiveKeyCommands.del(authorityHashKey).replaceWithVoid());
                if (username != null && !username.isBlank()) {
                    chain = chain.chain(() -> reactiveKeyCommands.del(props.usernameToUserIdKey(username)).replaceWithVoid());
                }
                if (roleHash != null && !roleHash.isBlank()) {
                    chain = chain.chain(() -> decrementRefAndMaybeDeleteRoleAsync(roleHash));
                }
                if (permissionHash != null && !permissionHash.isBlank()) {
                    chain = chain.chain(() -> decrementRefAndMaybeDeletePermissionAsync(permissionHash));
                }
                return chain;
            });
    }

    private Uni<Void> upsertRoleSetAsync(String roleHash, Set<String> roles) {
        if (roles.isEmpty()) {
            return Uni.createFrom().voidItem();
        }
        var roleHashKey = props.roleHashKey(roleHash);
        return reactiveKeyCommands.exists(roleHashKey).chain(exists -> {
            if (!exists) {
                return reactiveSetCommands.sadd(roleHashKey, roles.toArray(new String[0]))
                    .chain(() -> reactiveRefCountCommands.set(props.roleRefCountKey(roleHash), 1L))
                    .replaceWithVoid();
            }
            return reactiveRefCountCommands.incr(props.roleRefCountKey(roleHash)).replaceWithVoid();
        });
    }

    private Uni<Void> upsertPermissionSetAsync(String permissionHash, Set<String> permissions) {
        if (permissions.isEmpty()) {
            return Uni.createFrom().voidItem();
        }
        var permissionHashKey = props.permissionHashKey(permissionHash);
        return reactiveKeyCommands.exists(permissionHashKey).chain(exists -> {
            if (!exists) {
                return reactiveSetCommands.sadd(permissionHashKey, permissions.toArray(new String[0]))
                    .chain(() -> reactiveRefCountCommands.set(props.permissionRefCountKey(permissionHash), 1L))
                    .replaceWithVoid();
            }
            return reactiveRefCountCommands.incr(props.permissionRefCountKey(permissionHash)).replaceWithVoid();
        });
    }

    private Uni<Void> decrementRefAndMaybeDeleteRoleAsync(String roleHash) {
        var roleHashKey = props.roleHashKey(roleHash);
        return reactiveKeyCommands.exists(roleHashKey).chain(exists -> {
            if (!exists) {
                return Uni.createFrom().voidItem();
            }
            var refKey = props.roleRefCountKey(roleHash);
            return reactiveRefCountCommands.decr(refKey).chain(count -> {
                if (count <= 0) {
                    return reactiveKeyCommands.del(roleHashKey)
                        .chain(() -> reactiveKeyCommands.del(refKey))
                        .replaceWithVoid();
                }
                return Uni.createFrom().voidItem();
            });
        });
    }

    private Uni<Void> decrementRefAndMaybeDeletePermissionAsync(String permissionHash) {
        var permissionHashKey = props.permissionHashKey(permissionHash);
        return reactiveKeyCommands.exists(permissionHashKey).chain(exists -> {
            if (!exists) {
                return Uni.createFrom().voidItem();
            }
            var refKey = props.permissionRefCountKey(permissionHash);
            return reactiveRefCountCommands.decr(refKey).chain(count -> {
                if (count <= 0) {
                    return reactiveKeyCommands.del(permissionHashKey)
                        .chain(() -> reactiveKeyCommands.del(refKey))
                        .replaceWithVoid();
                }
                return Uni.createFrom().voidItem();
            });
        });
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
