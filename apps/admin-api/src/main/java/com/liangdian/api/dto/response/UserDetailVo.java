package com.liangdian.api.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record UserDetailVo(
    Long userid,
    String username,
    String nickname,
    Set<String> permissions,
    Set<String> roleCodes,
    String authorityKey
) {
    @JsonProperty
    public Map<String, List<String>> groupedPermission() {
        return permissions == null ? Map.of() : permissions.stream()
            .filter(perm -> perm != null && perm.contains(":"))
            .collect(Collectors.groupingBy(
                perm -> perm.substring(0, perm.indexOf(':')),
                Collectors.toList()
            ));
    }

    @JsonIgnore
    public Object getUserId() {
        return userid;
    }

    public static String encodeAuthorityKey(Set<String> permissions, Set<String> roleCodes) {
        var permissionPart = permissions == null ? "" : permissions.stream().sorted().collect(Collectors.joining(","));
        var rolePart = roleCodes == null ? "" : roleCodes.stream().sorted().collect(Collectors.joining(","));
        return Base64.getEncoder().encodeToString((rolePart + "|" + permissionPart).getBytes(StandardCharsets.UTF_8));
    }
}
