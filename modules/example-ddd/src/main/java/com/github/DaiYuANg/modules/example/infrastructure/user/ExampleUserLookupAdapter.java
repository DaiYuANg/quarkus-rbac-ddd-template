package com.github.DaiYuANg.modules.example.infrastructure.user;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.example.application.port.ExampleUserLookupPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExampleUserLookupAdapter implements ExampleUserLookupPort {

  private final UserRepository userRepository;

  @Override
  public void requireExistingUser(String username) {
    userRepository
        .findByUsername(username)
        .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND, "buyer user not found"));
  }
}
