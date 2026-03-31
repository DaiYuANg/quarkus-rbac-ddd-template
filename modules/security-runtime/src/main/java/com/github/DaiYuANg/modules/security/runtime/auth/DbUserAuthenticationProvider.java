package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.security.runtime.identity.AdminDbUserAuthoritySupport;
import com.github.DaiYuANg.security.auth.PasswordHasher;
import com.github.DaiYuANg.security.auth.UsernamePasswordAuthenticationRequest;
import com.github.DaiYuANg.security.identity.QuarkusSecurityIdentityFactory;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Priority(200)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class DbUserAuthenticationProvider
    implements IdentityProvider<UsernamePasswordAuthenticationRequest> {
  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final AdminDbUserAuthoritySupport userAuthoritySupport;
  private final QuarkusSecurityIdentityFactory securityIdentityFactory;

  @Override
  public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
    return UsernamePasswordAuthenticationRequest.class;
  }

  @Override
  public Uni<SecurityIdentity> authenticate(
      @NonNull UsernamePasswordAuthenticationRequest request, AuthenticationRequestContext context) {
    return context.runBlocking(() -> authenticateBlocking(request));
  }

  private SecurityIdentity authenticateBlocking(
      @NonNull UsernamePasswordAuthenticationRequest request) {
    return userRepository
        .findByUsername(request.username())
        .map(
            user -> {
              if (!passwordHasher.verify(request.password(), user.password)) {
                log.atDebug()
                    .addKeyValue("username", request.username())
                    .log("db-user: password mismatch");
                throw new BizException(ResultCode.USERNAME_OR_PASSWORD_INVALID);
              }
              if (user.userStatus != UserStatus.ENABLED) {
                log.atDebug()
                    .addKeyValue("username", request.username())
                    .log("db-user: user disabled");
                throw new BizException(ResultCode.USER_ACCESS_BLOCKED);
              }
              log.atDebug()
                  .addKeyValue("username", request.username())
                  .log("db-user: authenticated");
              return securityIdentityFactory.create(userAuthoritySupport.authenticatedUser(user));
            })
        .orElseGet(
            () -> {
              log.atDebug()
                  .addKeyValue("username", request.username())
                  .log("db-user: user not found, abstain");
              return null;
            });
  }
}
