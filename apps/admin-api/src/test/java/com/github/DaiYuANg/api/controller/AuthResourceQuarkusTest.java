package com.github.DaiYuANg.api.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.api.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.application.auth.AuthApplicationService;
import com.github.DaiYuANg.application.permission.PermissionCatalogLoader;
import io.quarkus.test.InjectMock;
import com.github.DaiYuANg.testsupport.ValkeyTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(ValkeyTestResource.class)
@TestProfile(AuthResourceQuarkusTest.AuthResourceIntegrationProfile.class)
class AuthResourceQuarkusTest {

  @InjectMock AuthApplicationService authApplicationService;
  @InjectMock PermissionCatalogLoader permissionCatalogLoader;

  @Test
  void loginReturnsTokenAndSetsRefreshCookie() {
    when(authApplicationService.login(any()))
        .thenReturn(new SystemAuthenticationToken("access-login", "rt-login", "Bearer", 120L, "v1"));

    given()
        .contentType("application/json")
        .body(
            """
            {
              "username": "root",
              "password": "root"
            }
            """)
        .when()
        .post("/api/v1/auth/login")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.accessToken", equalTo("access-login"))
        .body("data.refreshToken", equalTo("rt-login"))
        .header("Set-Cookie", containsString("refresh_token=rt-login"))
        .header("Set-Cookie", containsString("HttpOnly"))
        .header("Set-Cookie", containsString("Path=/api/v1/auth"));
  }

  @Test
  void refreshPrefersHeaderTokenOverCookieToken() {
    when(authApplicationService.refreshToken("rt-header"))
        .thenReturn(new SystemAuthenticationToken("access-header", "rt-next", "Bearer", 120L, "v2"));

    given()
        .contentType("application/json")
        .body("{}")
        .header("X-Refresh-Token", "rt-header")
        .cookie("refresh_token", "rt-cookie")
        .when()
        .post("/api/v1/auth/auth-refresh")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.accessToken", equalTo("access-header"))
        .body("data.refreshToken", equalTo("rt-next"));

    verify(authApplicationService).refreshToken("rt-header");
  }

  @Test
  void refreshUsesCookieTokenWhenHeaderMissing() {
    when(authApplicationService.refreshToken("rt-cookie"))
        .thenReturn(new SystemAuthenticationToken("access-cookie", "rt-next-2", "Bearer", 120L, "v3"));

    given()
        .contentType("application/json")
        .body("{}")
        .cookie("refresh_token", "rt-cookie")
        .when()
        .post("/api/v1/auth/auth-refresh")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.accessToken", equalTo("access-cookie"));

    verify(authApplicationService).refreshToken("rt-cookie");
  }

  public static class AuthResourceIntegrationProfile implements QuarkusTestProfile {
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
          Map.entry("quarkus.redis.hosts", "${test.redis.hosts}"),
          Map.entry("quarkus.log.console.json.enabled", "false"),
          Map.entry("quarkus.otel.enabled", "false"));
    }
  }
}
