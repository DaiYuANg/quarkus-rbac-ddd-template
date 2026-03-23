package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.security.PermissionSnapshot;
import io.quarkus.infinispan.client.Remote;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.infinispan.client.hotrod.RemoteCache;

@ApplicationScoped
public class PermissionSnapshotStore {
    private static final String CACHE_NAME = "rbac-auth";

    private final RemoteCache<String, CacheValue> cache;

    @Inject
    public PermissionSnapshotStore(@Remote(CACHE_NAME) RemoteCache<String, CacheValue> cache) {
        this.cache = cache;
    }

    public void save(PermissionSnapshot snapshot, Duration ttl) {
        cache.put(key(snapshot.username()), new CacheValue(encode(snapshot)), ttl.toSeconds(), TimeUnit.SECONDS);
    }

    public Optional<PermissionSnapshot> get(String username) {
        var value = cache.get(key(username));
        if (value == null || value.data() == null || value.data().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(decode(username, value.data()));
    }

    public void delete(String username) {
        cache.remove(key(username));
    }

    private String key(String username) {
        return "auth:permission-snapshot:" + username;
    }

    private String encode(PermissionSnapshot snapshot) {
        return String.join("|",
            escape(snapshot.username()),
            escape(snapshot.displayName()),
            escape(snapshot.userType()),
            escape(String.join(",", snapshot.roles())),
            escape(String.join(",", snapshot.permissions())),
            escape(snapshot.authorityVersion())
        );
    }

    private PermissionSnapshot decode(String username, String raw) {
        String[] parts = raw.split("\\|", -1);
        String displayName = parts.length > 1 ? unescape(parts[1]) : username;
        String userType = parts.length > 2 ? unescape(parts[2]) : "ADMIN";
        Set<String> roles = parts.length > 3 ? splitCsv(unescape(parts[3])) : Set.of();
        Set<String> permissions = parts.length > 4 ? splitCsv(unescape(parts[4])) : Set.of();
        String authorityVersion = parts.length > 5 ? unescape(parts[5]) : "";
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("displayName", displayName);
        attributes.put("userType", userType);
        attributes.put("roles", roles);
        attributes.put("permissions", permissions);
        attributes.put("authorityVersion", authorityVersion);
        return new PermissionSnapshot(username, displayName, userType, roles, permissions, authorityVersion, attributes);
    }

    private Set<String> splitCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String item : raw.split(",")) {
            if (!item.isBlank()) {
                result.add(item.trim());
            }
        }
        return Set.copyOf(result);
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("%", "%25").replace("|", "%7C").replace(",", "%2C");
    }

    private String unescape(String value) {
        return value == null ? "" : value.replace("%2C", ",").replace("%7C", "|").replace("%25", "%");
    }
}
