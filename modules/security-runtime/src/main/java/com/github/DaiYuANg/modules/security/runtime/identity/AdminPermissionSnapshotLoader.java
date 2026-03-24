package com.github.DaiYuANg.modules.security.runtime.identity;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotLoader;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminPermissionSnapshotLoader implements PermissionSnapshotLoader {
    private final UserRepository userRepository;
    private final AuthorityVersionStore authorityVersionStore;

    @ConfigProperty(name = "app.identity.db-user-type", defaultValue = "ADMIN")
    String dbUserType;

    @Override
    @Transactional
    public Optional<PermissionSnapshot> load(String username) {
        var user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        var roles = roleCodes(user);
        var permissions = permissionIdentifiers(user);
        var attributes = new LinkedHashMap<String, Object>();
        attributes.put("source", "db");
        attributes.put("displayName", user.nickname);
        attributes.put("roles", roles);
        attributes.put("permissions", permissions);
        attributes.put("authorityVersion", authorityVersionStore.versionFor(username));
        return Optional.of(new PermissionSnapshot(
            user.username,
            user.nickname == null || user.nickname.isBlank() ? user.username : user.nickname,
            dbUserType,
            roles,
            permissions,
            authorityVersionStore.versionFor(username),
            attributes,
            user.id
        ));
    }

    private LinkedHashSet<String> permissionIdentifiers(com.github.DaiYuANg.identity.entity.SysUser user) {
        return user.roles.stream()
            .flatMap(r -> r.permissionGroups.stream())
            .flatMap(g -> g.permissions.stream())
            .map(p -> p.code)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private LinkedHashSet<String> roleCodes(com.github.DaiYuANg.identity.entity.SysUser user) {
        return user.roles.stream()
            .map(r -> r.code)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
