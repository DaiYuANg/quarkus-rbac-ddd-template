package com.github.DaiYuANg.testsupport;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Shared test profile: Testcontainers PostgreSQL JDBC + Valkey from {@link ValkeyTestResource}
 * ({@code test.redis.hosts}), Hibernate {@code drop-and-create}. Use for full-stack HTTP tests that
 * must not mock core services.
 */
public final class QuarkusPostgresValkeyTestProfile implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.ofEntries(
        Map.entry("quarkus.datasource.jdbc.url", "jdbc:tc:postgresql:16-alpine:///rbac_test"),
        Map.entry(
            "quarkus.datasource.jdbc.driver", "org.testcontainers.jdbc.ContainerDatabaseDriver"),
        Map.entry("quarkus.datasource.jdbc.acquisition-timeout", "60S"),
        Map.entry("quarkus.datasource.username", "postgres"),
        Map.entry("quarkus.datasource.password", "postgres"),
        Map.entry("quarkus.hibernate-orm.schema-management.strategy", "drop-and-create"),
        Map.entry("quarkus.hibernate-orm.sql-load-script", "import-test.sql"),
        Map.entry("quarkus.redis.hosts", "${test.redis.hosts}"),
        Map.entry("quarkus.redis.devservices.enabled", "false"),
        Map.entry("quarkus.http.test-port", "0"),
        // Main app YAML uses a separate management port; disable for tests so /q/health/* is on the
        // test HTTP port.
        Map.entry("quarkus.management.enabled", "false"),
        Map.entry("quarkus.log.console.json.enabled", "false"),
        Map.entry("quarkus.otel.enabled", "false"));
  }
}
