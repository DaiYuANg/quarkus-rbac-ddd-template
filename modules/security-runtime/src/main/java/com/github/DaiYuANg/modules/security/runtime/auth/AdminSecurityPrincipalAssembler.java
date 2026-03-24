package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminSecurityPrincipalAssembler {

    @ConfigProperty(name = "app.identity.db-user-type", defaultValue = "ADMIN")
    String dbUserType;

    public AuthenticatedUser fromDbUser(SysUser user) {
        var roles = roleCodes(user);
        var permissions = permissionIdentifiers(user);
        var attributes = new LinkedHashMap<String, Object>();
        attributes.put("source", "db");
        attributes.put("nickname", user.nickname);
        attributes.put("permissions", permissions);
        attributes.put("roles", roles);
        return new AuthenticatedUser(
            user.username,
            user.nickname == null || user.nickname.isBlank() ? user.username : user.nickname,
            dbUserType,
            roles,
            permissions,
            attributes,
            user.id
        );
    }

    private LinkedHashSet<String> permissionIdentifiers(SysUser user) {
        return user.roles.stream()
            .flatMap(r -> r.permissionGroups.stream())
            .flatMap(g -> g.permissions.stream())
            .map(p -> p.code)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private LinkedHashSet<String> roleCodes(SysUser user) {
        return user.roles.stream()
            .map(r -> r.code)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
