package com.github.DaiYuANg.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.security.auth.AuthenticationProviderResult;
import com.github.DaiYuANg.security.auth.AuthenticationProviders;
import com.github.DaiYuANg.security.auth.AuthenticationResult;
import com.github.DaiYuANg.security.auth.DefaultAuthenticationFailureHandler;
import com.github.DaiYuANg.security.auth.DefaultAuthenticationSuccessHandler;
import com.github.DaiYuANg.security.auth.LoginAuthenticationManager;
import com.github.DaiYuANg.security.auth.LoginAuthenticationProvider;
import com.github.DaiYuANg.security.auth.LoginAuthenticationRequest;
import com.github.DaiYuANg.security.auth.UsernamePasswordAuthenticationRequest;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LoginAuthenticationManagerIntegrationTest {

  @Test
  void providerChainReturnsFirstSuccessAfterAbstainAndFailure() {
    var providers = mock(AuthenticationProviders.class);
    when(providers.orderedProviders())
        .thenReturn(
            List.of(
                new AbstainProvider(),
                new FailureProvider(),
                new SuccessProvider("chain-user"),
                new SuccessProvider("should-not-reach")));

    var manager =
        new LoginAuthenticationManager(
            providers, new DefaultAuthenticationSuccessHandler(), new DefaultAuthenticationFailureHandler());

    var result =
        manager.authenticate(new UsernamePasswordAuthenticationRequest("chain-user", "password"));

    assertEquals("chain-user", result.user().username());
    assertEquals("success-provider", result.providerId());
  }

  @Test
  void providerChainThrowsWhenAllProvidersAbstain() {
    var providers = mock(AuthenticationProviders.class);
    when(providers.orderedProviders()).thenReturn(List.of(new AbstainProvider(), new AbstainProvider()));

    var manager =
        new LoginAuthenticationManager(
            providers, new DefaultAuthenticationSuccessHandler(), new DefaultAuthenticationFailureHandler());

    var ex =
        assertThrows(
            BizException.class,
            () -> manager.authenticate(new UsernamePasswordAuthenticationRequest("u", "p")));

    assertEquals(ResultCode.USERNAME_OR_PASSWORD_INVALID, ex.getResultCode());
  }

  private static final class AbstainProvider
      implements LoginAuthenticationProvider<UsernamePasswordAuthenticationRequest> {
    @Override
    public String providerId() {
      return "abstain-provider";
    }

    @Override
    public boolean supports(LoginAuthenticationRequest request) {
      return request instanceof UsernamePasswordAuthenticationRequest;
    }

    @Override
    public AuthenticationProviderResult authenticate(UsernamePasswordAuthenticationRequest request) {
      return AuthenticationProviderResult.abstain();
    }
  }

  private static final class FailureProvider
      implements LoginAuthenticationProvider<UsernamePasswordAuthenticationRequest> {
    @Override
    public String providerId() {
      return "failure-provider";
    }

    @Override
    public boolean supports(LoginAuthenticationRequest request) {
      return request instanceof UsernamePasswordAuthenticationRequest;
    }

    @Override
    public AuthenticationProviderResult authenticate(UsernamePasswordAuthenticationRequest request) {
      return AuthenticationProviderResult.failure(ResultCode.USERNAME_OR_PASSWORD_INVALID);
    }
  }

  private static final class SuccessProvider
      implements LoginAuthenticationProvider<UsernamePasswordAuthenticationRequest> {
    private final String username;

    private SuccessProvider(String username) {
      this.username = username;
    }

    @Override
    public String providerId() {
      return "success-provider";
    }

    @Override
    public boolean supports(LoginAuthenticationRequest request) {
      return request instanceof UsernamePasswordAuthenticationRequest;
    }

    @Override
    public AuthenticationProviderResult authenticate(UsernamePasswordAuthenticationRequest request) {
      var user =
          new AuthenticatedUser(
              username, "Chain User", "ADMIN", Set.of("ops"), Set.of("user:view"), Map.of(), 1L);
      return AuthenticationProviderResult.success(new AuthenticationResult(user, providerId()));
    }
  }
}
