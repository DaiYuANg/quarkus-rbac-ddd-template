package com.github.DaiYuANg.security.authorization;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.security.access.CurrentUserAccess;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Arrays;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultAuthorizationService implements AuthorizationService {
  private final CurrentUserAccess currentUserAccess;
  private final AuthorizationAuditor authorizationAuditor;

  @Override
  public AuthorizationDecision decide(@NonNull String code) {
    return decide(PermissionDescriptor.ofCode(code));
  }

  @Override
  public AuthorizationDecision decide(@NonNull String resource, @NonNull String action) {
    return decide(PermissionDescriptor.of(resource, action));
  }

  @Override
  public AuthorizationDecision decide(@NonNull PermissionDescriptor descriptor) {
    val current = currentUserAccess.currentUser().orElse(null);
    if (current == null) {
      return AuthorizationDecision.deny(descriptor, null, "anonymous");
    }
    return current.permissions().contains(descriptor.code())
        ? AuthorizationDecision.allow(descriptor, current, "snapshot")
        : AuthorizationDecision.deny(descriptor, current, "permission-miss");
  }

  @Override
  public void check(@NonNull String code) {
    check(PermissionDescriptor.ofCode(code));
  }

  @Override
  public void check(@NonNull String resource, @NonNull String action) {
    check(PermissionDescriptor.of(resource, action));
  }

  @Override
  public void check(@NonNull PermissionDescriptor descriptor) {
    val decision = decide(descriptor);
    if (!decision.allowed()) {
      authorizationAuditor.auditDenied(decision);
      throw new BizException(ResultCode.FORBIDDEN);
    }
  }

  @Override
  public void checkAny(String... codes) {
    val decisions = codes == null ? java.util.List.<AuthorizationDecision>of() : Arrays.stream(codes).map(this::decide).toList();
    if (decisions.stream().anyMatch(AuthorizationDecision::allowed)) {
      return;
    }
    var lastDenied =
        decisions.stream()
            .filter(decision -> !decision.allowed())
            .reduce((first, second) -> second)
            .orElse(null);
    if (lastDenied == null) {
      lastDenied =
          AuthorizationDecision.deny(
              PermissionDescriptor.ofCode(""),
              currentUserAccess.currentUser().orElse(null),
              "no-permission-candidates");
    }
    authorizationAuditor.auditDenied(lastDenied);
    throw new BizException(ResultCode.FORBIDDEN);
  }

  @Override
  public void checkAll(String... codes) {
    if (codes == null) {
      return;
    }
    Arrays.stream(codes).forEach(this::check);
  }

  @Override
  public boolean isAllowed(@NonNull String code) {
    return decide(code).allowed();
  }

  @Override
  public boolean isAllowed(@NonNull String resource, @NonNull String action) {
    return decide(resource, action).allowed();
  }
}
