package com.github.DaiYuANg.testsupport;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import org.testcontainers.containers.GenericContainer;

public class ValkeyTestResource implements QuarkusTestResourceLifecycleManager {

  private GenericContainer<?> valkey;

  @Override
  public Map<String, String> start() {
    valkey = new GenericContainer<>("valkey/valkey:8-alpine").withExposedPorts(6379);
    valkey.start();
    var hosts = "redis://" + valkey.getHost() + ":" + valkey.getMappedPort(6379);
    System.out.println("Valkey test resource started at " + hosts);
    return Map.of("test.redis.hosts", hosts);
  }

  @Override
  public void stop() {
    if (valkey != null) {
      valkey.stop();
    }
  }
}
