package com.liangdian.security;

import com.liangdian.common.constant.ResultCode;
import com.liangdian.identity.constant.UserStatus;
import com.liangdian.identity.repository.UserRepository;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@Priority(200)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DbUserAuthenticationProvider implements LoginAuthenticationProvider<UsernamePasswordAuthenticationRequest> {
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
                    return AuthenticationProviderResult.failure(ResultCode.USERNAME_OR_PASSWORD_INVALID);
                }
                if (user.userStatus != UserStatus.ENABLED) {
                    return AuthenticationProviderResult.failure(ResultCode.USER_ACCESS_BLOCKED);
                }
                return AuthenticationProviderResult.success(new AuthenticationResult(principalAssembler.fromDbUser(user), providerId()));
            })
            .orElseGet(AuthenticationProviderResult::abstain);
    }
}
