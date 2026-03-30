package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminSecurityPrincipalAssembler {

  @ConfigProperty(name = "app.identity.db-user-type", defaultValue = "ADMIN")
  String dbUserType;

  private final UserRepository userRepository;

  public AuthenticatedUser fromDbUser(@NonNull SysUser user) {
    val roles = new LinkedHashSet<>(userRepository.findRoleCodesByUsername(user.username));
    val permissions =
        new LinkedHashSet<>(userRepository.findPermissionCodesByUsername(user.username));
    val attributes = new LinkedHashMap<String, Object>();
    attributes.put(PrincipalAttributeKeys.SOURCE, "db");
    attributes.put(PrincipalAttributeKeys.NICKNAME, user.nickname);
    attributes.put(PrincipalAttributeKeys.PERMISSIONS, permissions);
    attributes.put(PrincipalAttributeKeys.ROLES, roles);
    return new AuthenticatedUser(
        user.username,
        user.nickname == null || user.nickname.isBlank() ? user.username : user.nickname,
        dbUserType,
        roles,
        permissions,
        attributes,
        user.id);
  }
}
