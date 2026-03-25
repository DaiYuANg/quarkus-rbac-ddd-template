package com.github.DaiYuANg.security.auth;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.security.config.ConfigUserAccountConfig;
import com.github.DaiYuANg.security.config.ConfigUserAccounts;
import com.github.DaiYuANg.security.config.ConfigUserAuthorityId;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Priority(100)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class ConfigUserAuthenticationProvider
    implements LoginAuthenticationProvider<UsernamePasswordAuthenticationRequest> {
  private final ConfigUserAccountConfig config;
  private final PasswordHasher passwordHasher;

  @ConfigProperty(name = "app.identity.config-user-fallback-type", defaultValue = "CONFIG")
  String configUserFallbackType;

  @Override
  public String providerId() {
    return "config-user";
  }

  @Override
  public boolean supports(LoginAuthenticationRequest request) {
    return request instanceof UsernamePasswordAuthenticationRequest;
  }

  @Override
  public AuthenticationProviderResult authenticate(UsernamePasswordAuthenticationRequest request) {
    if (config.users() == null || config.users().isEmpty()) {
      log.atDebug().log("config-user: no users configured, abstain");
      return AuthenticationProviderResult.abstain();
    }
    var entry = ConfigUserAccounts.find(config, request.username()).orElse(null);
    if (entry == null) {
      log.atDebug()
          .addKeyValue("username", request.username())
          .log("config-user: user not found, abstain");
      return AuthenticationProviderResult.abstain();
    }
    if (!passwordHasher.verify(request.password(), entry.passwordHash())) {
      log.atDebug()
          .addKeyValue("username", request.username())
          .log("config-user: password mismatch");
      return AuthenticationProviderResult.failure(ResultCode.USERNAME_OR_PASSWORD_INVALID);
    }
    log.atDebug().addKeyValue("username", request.username()).log("config-user: authenticated");
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("source", "config");
    attributes.put("providerId", providerId());
    attributes.put("permissions", new LinkedHashSet<>(entry.permissions().orElseGet(List::of)));
    attributes.put("roles", new LinkedHashSet<>(entry.roles().orElseGet(List::of)));
    String principalType = entry.principalUserType().orElse(configUserFallbackType);
    return AuthenticationProviderResult.success(
        new AuthenticationResult(
            new AuthenticatedUser(
                entry.username(),
                entry.displayName().orElse(entry.username()),
                principalType,
                new LinkedHashSet<>(entry.roles().orElseGet(List::of)),
                new LinkedHashSet<>(entry.permissions().orElseGet(List::of)),
                attributes,
                ConfigUserAuthorityId.forUsername(entry.username())),
            providerId()));
  }
}
