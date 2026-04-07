package com.github.DaiYuANg.migrator;

import io.quarkus.runtime.QuarkusApplication;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MigrationApp implements QuarkusApplication {

  private final Flyway flyway;

  @Override
  public int run(String... args) {
    flyway.validate();
    flyway.migrate();
    return 0;
  }
}
