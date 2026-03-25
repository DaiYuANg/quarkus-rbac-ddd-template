package com.github.DaiYuANg.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.github.DaiYuANg.testsupport.QuarkusPostgresValkeyTestProfile;
import com.github.DaiYuANg.testsupport.ValkeyTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

/**
 * End-to-end HTTP flow: real {@code AuthApplicationService}, JWT, Redis, PostgreSQL (Testcontainers).
 * Uses the built-in <strong>config user</strong> {@code root} / {@code root} (no {@code sys_user} row).
 *
 * <p><strong>Refresh tokens</strong> are currently resolved only for <strong>database</strong> users in
 * {@code AdminRefreshTokenAuthenticationProvider}, so refresh is not asserted here.
 */
@QuarkusTest
@QuarkusTestResource(ValkeyTestResource.class)
@TestProfile(QuarkusPostgresValkeyTestProfile.class)
class AdminApiFullStackFlowIT {

  @Test
  void configUserLoginAndMe() {
    String access =
        given()
            .contentType("application/json")
            .body(
                """
                {"username":"root","password":"root"}
                """)
            .when()
            .post("/api/v1/auth/login")
            .then()
            .statusCode(200)
            .body("code", equalTo("00000"))
            .body("data.accessToken", notNullValue())
            .body("data.refreshToken", notNullValue())
            .body("data.tokenType", equalTo("Bearer"))
            .body("data.authorityVersion", notNullValue())
            .header("Set-Cookie", notNullValue())
            .extract()
            .path("data.accessToken");

    given()
        .header("Authorization", "Bearer " + access)
        .when()
        .get("/api/v1/me")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.id", equalTo("root"))
        // JWT claim mapping may expose displayName as subject until custom claims are mapped everywhere
        .body("data.name", notNullValue())
        .body("data.permissions", notNullValue());
  }

  @Test
  void livenessUp() {
    given().when().get("/q/health/live").then().statusCode(200);
  }
}
