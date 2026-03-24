package com.github.DaiYuANg.security.auth;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.repository.UserRepository;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Priority(200)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DbUserAuthenticationProvider implements LoginAuthenticationProvider<UsernamePasswordAuthenticationRequest> {
    private static final Logger log = LoggerFactory.getLogger(DbUserAuthenticationProvider.class);
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AdminSecurityPrincipalAssembler principalAssembler;

    @Override
    public String providerId() {
        return "db-user";
    }

    @Override
    public boolean supports(LoginAuthenticationRequest request) {
        return request instanceof UsernamePasswordAuthenticationRequest;
    }

    @Override
    public AuthenticationProviderResult authenticate(UsernamePasswordAuthenticationRequest request) {
        return userRepository.findByUsername(request.username())
            .map(user -> {
                if (!passwordHasher.verify(request.password(), user.password)) {
                    log.atDebug().addKeyValue("username", request.username()).log("db-user: password mismatch");
                    return AuthenticationProviderResult.failure(ResultCode.USERNAME_OR_PASSWORD_INVALID);
                }
                if (user.userStatus != UserStatus.ENABLED) {
                    log.atDebug().addKeyValue("username", request.username()).log("db-user: user disabled");
                    return AuthenticationProviderResult.failure(ResultCode.USER_ACCESS_BLOCKED);
                }
                log.atDebug().addKeyValue("username", request.username()).log("db-user: authenticated");
                return AuthenticationProviderResult.success(new AuthenticationResult(principalAssembler.fromDbUser(user), providerId()));
            })
            .orElseGet(() -> {
                log.atDebug().addKeyValue("username", request.username()).log("db-user: user not found, abstain");
                return AuthenticationProviderResult.abstain();
            });
    }
}
