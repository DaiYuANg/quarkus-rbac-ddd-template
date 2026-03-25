package com.github.DaiYuANg.security.auth;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordHasher {
  public String hash(String raw) {
    return BcryptUtil.bcryptHash(raw);
  }

  public boolean verify(String raw, String encoded) {
    return BcryptUtil.matches(raw, encoded);
  }
}
