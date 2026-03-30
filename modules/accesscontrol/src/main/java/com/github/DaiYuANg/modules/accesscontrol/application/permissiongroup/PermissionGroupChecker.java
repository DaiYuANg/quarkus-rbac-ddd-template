package com.github.DaiYuANg.modules.accesscontrol.application.permissiongroup;

import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.repository.PermissionGroupRepository;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.PermissionGroupCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdatePermissionGroupForm;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Consumer;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionGroupChecker {
  private final PermissionGroupRepository repository;

  public void ensureCreatable(@NonNull PermissionGroupCreationForm form) {
    Stream.of(
            new AvailabilityCheck(form.name(), null, this::ensureNameAvailable),
            new AvailabilityCheck(form.code(), null, this::ensureCodeAvailable))
        .filter(AvailabilityCheck::changed)
        .forEach(AvailabilityCheck::validate);
  }

  public void ensureUpdatable(
      @NonNull SysPermissionGroup group, @NonNull UpdatePermissionGroupForm form) {
    Stream.of(
            new AvailabilityCheck(form.name(), group.name, this::ensureNameAvailable),
            new AvailabilityCheck(form.code(), group.code, this::ensureCodeAvailable))
        .filter(AvailabilityCheck::changed)
        .forEach(AvailabilityCheck::validate);
  }

  private void ensureNameAvailable(@NonNull String name) {
    if (repository.countByName(name) > 0) {
      throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "permission group name already exists");
    }
  }

  private void ensureCodeAvailable(@NonNull String code) {
    if (repository.countByCode(code) > 0) {
      throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "permission group code already exists");
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
