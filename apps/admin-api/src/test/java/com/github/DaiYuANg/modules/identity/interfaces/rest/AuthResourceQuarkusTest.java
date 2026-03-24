package com.github.DaiYuANg.modules.identity.interfaces.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.modules.accesscontrol.application.permission.PermissionCatalogLoader;
import com.github.DaiYuANg.modules.identity.application.AuthApplicationService;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponse;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeRoleItem;
import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.testsupport.ValkeyTestResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  @Test
  @TestSecurity(user = "root", roles = {"user:view"})
  void meEndpointReturnsFrontendContractShape() {
    when(authApplicationService.me("root"))
        .thenReturn(
            new MeResponse(
                "1",
                "Root Admin",
                "root@example.com",
                List.of(new MeRoleItem("1", "admin")),
                Set.of("user:view", "role:view")));

    given()
        .when()
        .get("/api/v1/me")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.id", equalTo("1"))
        .body("data.name", equalTo("Root Admin"))
        .body("data.email", equalTo("root@example.com"))
        .body("data.roles.size()", equalTo(1))
        .body("data.roles[0].id", equalTo("1"))
        .body("data.roles[0].name", equalTo("admin"))
        .body("data.permissions.size()", equalTo(2));
  }

  @Test
  void refreshAliasUsesSameFlowAsAuthRefresh() {
    when(authApplicationService.refreshToken("rt-cookie-alias"))
        .thenReturn(new SystemAuthenticationToken("access-alias", "rt-next-alias", "Bearer", 120L, "v4"));

    given()
        .contentType("application/json")
        .body("{}")
        .cookie("refresh_token", "rt-cookie-alias")
        .when()
        .post("/api/v1/auth/refresh")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.accessToken", equalTo("access-alias"));

    verify(authApplicationService).refreshToken("rt-cookie-alias");
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
