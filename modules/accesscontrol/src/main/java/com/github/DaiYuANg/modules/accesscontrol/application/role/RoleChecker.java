package com.github.DaiYuANg.modules.accesscontrol.application.role;

import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.repository.RoleRepository;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.RoleCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdateRoleForm;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Consumer;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RoleChecker {
  private final RoleRepository roleRepository;

  public void ensureCreatable(@NonNull RoleCreationForm form) {
    Stream.of(
            new AvailabilityCheck(form.name(), null, this::ensureNameAvailable),
            new AvailabilityCheck(form.code(), null, this::ensureCodeAvailable))
        .filter(AvailabilityCheck::changed)
        .forEach(AvailabilityCheck::validate);
  }

  public void ensureUpdatable(@NonNull SysRole role, @NonNull UpdateRoleForm form) {
    Stream.of(
            new AvailabilityCheck(form.name(), role.name, this::ensureNameAvailable),
            new AvailabilityCheck(form.code(), role.code, this::ensureCodeAvailable))
        .filter(AvailabilityCheck::changed)
        .forEach(AvailabilityCheck::validate);
  }

  private void ensureNameAvailable(@NonNull String name) {
    if (roleRepository.countByName(name) > 0) {
      throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "role name already exists");
    }
  }

  private void ensureCodeAvailable(@NonNull String code) {
    if (roleRepository.countByCode(code) > 0) {
      throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "role code already exists");
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
