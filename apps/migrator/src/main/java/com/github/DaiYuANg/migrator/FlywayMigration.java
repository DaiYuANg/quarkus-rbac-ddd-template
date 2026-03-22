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

    @RequiredArgsConstructor(onConstructor = @__(@Inject))
    public static class MigrationApp implements QuarkusApplication {

        private final Flyway flyway;

        @Override
        public int run(String... args) {
            flyway.migrate();
            return 0;
        }
    }
}