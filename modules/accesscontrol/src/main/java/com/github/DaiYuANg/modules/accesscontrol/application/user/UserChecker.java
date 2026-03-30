package com.github.DaiYuANg.modules.accesscontrol.application.user;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.repository.UserRepository;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdateUserForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UserCreationForm;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Consumer;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserChecker {
  private final UserRepository userRepository;

  public void ensureCreatable(@NonNull UserCreationForm form) {
    Stream.of(
            new AvailabilityCheck(form.username(), null, this::ensureUsernameAvailable),
            new AvailabilityCheck(form.email(), null, this::ensureEmailAvailable),
            new AvailabilityCheck(form.mobilePhone(), null, this::ensureMobilePhoneAvailable))
        .filter(AvailabilityCheck::changed)
        .forEach(AvailabilityCheck::validate);
  }

  public void ensureUpdatable(@NonNull SysUser user, @NonNull UpdateUserForm form) {
    Stream.of(
            new AvailabilityCheck(form.username(), user.username, this::ensureUsernameAvailable),
            new AvailabilityCheck(form.email(), user.email, this::ensureEmailAvailable),
            new AvailabilityCheck(
                form.mobilePhone(), user.mobilePhone, this::ensureMobilePhoneAvailable))
        .filter(AvailabilityCheck::changed)
        .forEach(AvailabilityCheck::validate);
  }

  private void ensureUsernameAvailable(@NonNull String username) {
    if (userRepository.countByUsername(username) > 0) {
      throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "username already exists");
    }
  }

  private void ensureEmailAvailable(@NonNull String email) {
    if (userRepository.countByEmail(email) > 0) {
      throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "email already exists");
    }
  }

  private void ensureMobilePhoneAvailable(@NonNull String mobilePhone) {
    if (userRepository.countByMobilePhone(mobilePhone) > 0) {
      throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "mobilePhone already exists");
    }
  }

  private record AvailabilityCheck(String next, String current, Consumer<String> validator) {
    private boolean changed() {
      return next != null && !Objects.equals(next, current);
    }

    private void validate() {
      validator.accept(next);
    }
  }
}
