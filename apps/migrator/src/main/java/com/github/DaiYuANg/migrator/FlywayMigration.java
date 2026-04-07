package com.github.DaiYuANg.migrator;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;

@QuarkusMain
public class FlywayMigration {
  static void main(String... args) {
    Quarkus.run(MigrationApp.class, args);
  }
}
