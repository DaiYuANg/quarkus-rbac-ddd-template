package com.github.DaiYuANg.cache.calculator;

import java.util.Set;

/**
 * Computes deterministic hashes for role/permission sets for deduplication.
 */
public interface AuthorityHashCalculator {

    String generateAuthorityKey(Set<String> roleCodes, Set<String> permissions);

    String generateRoleHash(Set<String> roleCodes);

    String generatePermissionHash(Set<String> permissions);

    /**
     * Concatenate role codes and permission codes in sorted order for hashing.
     */
    static String concatForHash(Set<String> roleCodes, Set<String> permissions) {
        var roles = roleCodes == null || roleCodes.isEmpty()
            ? ""
            : roleCodes.stream().sorted().reduce((a, b) -> a + "|" + b).orElse("");
        var perms = permissions == null || permissions.isEmpty()
            ? ""
            : permissions.stream().sorted().reduce((a, b) -> a + "|" + b).orElse("");
        return roles + "||" + perms;
    }
}
