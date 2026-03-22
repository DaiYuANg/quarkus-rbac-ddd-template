package com.github.DaiYuANg.security;

import com.github.DaiYuANg.common.constant.ResultCode;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@Priority(100)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ConfigUserAuthenticationProvider implements LoginAuthenticationProvider<UsernamePasswordAuthenticationRequest> {
    private final ConfigUserAccountConfig config;
    private final PasswordHasher passwordHasher;

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
            return AuthenticationProviderResult.abstain();
        }
        var entry = config.users().values().stream()
            .filter(user -> user.username().equalsIgnoreCase(request.username()))
            .findFirst()
            .orElse(null);
        if (entry == null) {
            return AuthenticationProviderResult.abstain();
        }
        if (!passwordHasher.verify(request.password(), entry.passwordHash())) {
            return AuthenticationProviderResult.failure(ResultCode.USERNAME_OR_PASSWORD_INVALID);
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("source", "config");
        attributes.put("providerId", providerId());
        attributes.put("permissions", new LinkedHashSet<>(entry.permissions().orElseGet(List::of)));
        attributes.put("roles", new LinkedHashSet<>(entry.roles().orElseGet(List::of)));
        return AuthenticationProviderResult.success(new AuthenticationResult(new AuthenticatedUser(
            entry.username(),
            entry.displayName().orElse(entry.username()),
            "CONFIG",
            new LinkedHashSet<>(entry.roles().orElseGet(List::of)),
            new LinkedHashSet<>(entry.permissions().orElseGet(List::of)),
            attributes
        ), providerId()));
    }
}
