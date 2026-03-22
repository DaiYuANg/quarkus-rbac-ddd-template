package com.liangdian.redis;

import com.liangdian.security.PermissionSnapshot;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class PermissionSnapshotStore {
    private final ValueCommands<String, String> commands;
    private final KeyCommands<String> keyCommands;

    @Inject
    public PermissionSnapshotStore(RedisDataSource dataSource) {
        this.commands = dataSource.value(String.class);
        this.keyCommands = dataSource.key();
    }

    public void save(PermissionSnapshot snapshot, Duration ttl) {
        commands.setex(key(snapshot.username()), ttl.getSeconds(), encode(snapshot));
    }

    public Optional<PermissionSnapshot> get(String username) {
        var raw = commands.get(key(username));
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(decode(username, raw));
    }

    public void delete(String username) {
        keyCommands.del(key(username));
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
