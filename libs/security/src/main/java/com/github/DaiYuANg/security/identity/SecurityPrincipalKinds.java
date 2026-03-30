package com.github.DaiYuANg.security.identity;

public interface SecurityPrincipalKinds {
  interface UserType {
    String SUPER_ADMIN = "SUPER_ADMIN";
  }

  interface Source {
    String DB = "db";
    String SUPER_ADMIN = "super-admin";
  }

  interface Provider {
    String DB_USER = "db-user";
    String SUPER_ADMIN = "super-admin";
  }

  interface Role {
    String SUPER_ADMIN = "super-admin";
  }
}
