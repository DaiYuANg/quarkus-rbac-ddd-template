package com.github.DaiYuANg.security.identity;

import com.github.DaiYuANg.application.converter.ViewMapper;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotLoader;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminPermissionSnapshotLoader implements PermissionSnapshotLoader {
    private final UserRepository userRepository;
    private final ViewMapper viewMapper;
    private final AuthorityVersionStore authorityVersionStore;

    @Override
    @Transactional
    public Optional<PermissionSnapshot> load(String username) {
        var user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        var roles = new LinkedHashSet<>(viewMapper.roleCodes(user));
        var permissions = new LinkedHashSet<>(viewMapper.permissionIdentifiers(user));
        var attributes = new LinkedHashMap<String, Object>();
        attributes.put("source", "db");
        attributes.put("displayName", user.nickname);
        attributes.put("roles", roles);
        attributes.put("permissions", permissions);
        attributes.put("authorityVersion", authorityVersionStore.versionFor(username));
        return Optional.of(new PermissionSnapshot(
            user.username,
            user.nickname == null || user.nickname.isBlank() ? user.username : user.nickname,
            "ADMIN",
            roles,
            permissions,
            authorityVersionStore.versionFor(username),
            attributes,
            user.id
        ));
    }
}
