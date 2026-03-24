package com.github.DaiYuANg.security.token;

import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class PrincipalAttributesSerializer {
    public Map<String, Object> toAttributes(AuthenticatedUser user) {
        var attributes = new LinkedHashMap<String, Object>();
        if (user.attributes() != null) {
            attributes.putAll(user.attributes());
        }
        attributes.put("subject", user.username());
        attributes.put("displayName", user.displayName());
        attributes.put("userType", user.userType());
        attributes.put("roles", immutableCopy(user.roles()));
        attributes.put("permissions", immutableCopy(user.permissions()));
        return attributes;
    }


    public Map<String, Object> deserialize(Map<String, Object> attributes) {
        return attributes == null ? Map.of() : new LinkedHashMap<>(attributes);
    }

    @SuppressWarnings("unchecked")
    public CurrentAuthenticatedUser toCurrentUser(Map<String, Object> attributes, String principalName) {
        var values = attributes == null ? Map.<String, Object>of() : attributes;
        return new CurrentAuthenticatedUser(
            stringValue(values.getOrDefault("subject", principalName), principalName),
            stringValue(values.getOrDefault("displayName", principalName), principalName),
            stringValue(values.getOrDefault("userType", "SYSTEM"), "SYSTEM"),
            asSet(values.get("roles")),
            asSet(values.get("permissions")),
            values
        );
    }

    private Set<String> asSet(Object value) {
        if (value instanceof Set<?> set) {
            return set.stream().map(String::valueOf).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }
        if (value instanceof Iterable<?> iterable) {
            var result = new LinkedHashSet<String>();
            iterable.forEach(item -> result.add(String.valueOf(item)));
            return result;
        }
        if (value == null) {
            return Set.of();
        }
        return Set.of(String.valueOf(value));
    }

    private Set<String> immutableCopy(Set<String> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }

    private String stringValue(Object value, String defaultValue) {
        return value == null ? defaultValue : String.valueOf(value);
    }
}
