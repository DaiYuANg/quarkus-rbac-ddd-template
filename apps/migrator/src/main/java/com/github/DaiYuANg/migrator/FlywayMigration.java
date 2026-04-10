package com.github.DaiYuANg.migrator;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class FlywayMigration {
  public static void main(String... args) {
    Quarkus.run(MigrationApp.class, args);
  }
}
