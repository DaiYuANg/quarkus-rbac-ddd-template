package com.github.DaiYuANg.cache.calculator;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;
import lombok.val;
import org.apache.commons.codec.digest.DigestUtils;

@ApplicationScoped
public class Sha256AuthorityHashCalculator implements AuthorityHashCalculator {

  private static final String SEPARATOR = "|";

  @Override
  public String generateAuthorityKey(Set<String> roleCodes, Set<String> permissions) {
    return DigestUtils.sha256Hex(AuthorityHashCalculator.concatForHash(roleCodes, permissions));
  }

  @Override
  public String generateRoleHash(Set<String> roleCodes) {
    val target =
        roleCodes == null || roleCodes.isEmpty()
            ? ""
            : roleCodes.stream().sorted().reduce((a, b) -> a + SEPARATOR + b).orElse("");
    return DigestUtils.sha256Hex(target);
  }

  @Override
  public String generatePermissionHash(Set<String> permissions) {
    val target =
        permissions == null || permissions.isEmpty()
            ? ""
            : permissions.stream().sorted().reduce((a, b) -> a + SEPARATOR + b).orElse("");
    return DigestUtils.sha256Hex(target);
  }
}
