package com.github.DaiYuANg.security.auth;

import com.github.DaiYuANg.application.converter.ViewMapper;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminSecurityPrincipalAssembler {
    private final ViewMapper viewMapper;

    public AuthenticatedUser fromDbUser(SysUser user) {
        var roles = new LinkedHashSet<>(viewMapper.roleCodes(user));
        var permissions = new LinkedHashSet<>(viewMapper.permissionIdentifiers(user));
        var attributes = new LinkedHashMap<String, Object>();
        attributes.put("source", "db");
        attributes.put("nickname", user.nickname);
        attributes.put("permissions", permissions);
        attributes.put("roles", roles);
        return new AuthenticatedUser(
            user.username,
            user.nickname == null || user.nickname.isBlank() ? user.username : user.nickname,
            "ADMIN",
            roles,
            permissions,
            attributes,
            user.id
        );
    }
}
