package com.github.DaiYuANg.mobile.integration;

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
 * Full-stack mobile flow for the {@code mobile-member} config user (password {@code root}). Refresh
 * is omitted: refresh resolution is DB-user-only in the current security provider (same as admin
 * IT).
 */
@QuarkusTest
@QuarkusTestResource(ValkeyTestResource.class)
@TestProfile(QuarkusPostgresValkeyTestProfile.class)
class MobileApiFullStackFlowIT {

  @Test
  void configMemberLoginAndMe() {
    String access =
        given()
            .contentType("application/json")
            .body(
                """
                {"username":"mobile-member","password":"root"}
                """)
            .when()
            .post("/api/mobile/v1/auth/login")
            .then()
            .statusCode(200)
            .body("code", equalTo("00000"))
            .body("data.accessToken", notNullValue())
            .body("data.refreshToken", notNullValue())
            .extract()
            .path("data.accessToken");

    given()
        .header("Authorization", "Bearer " + access)
        .when()
        .get("/api/mobile/v1/me")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.id", equalTo("mobile-member"))
        .body("data.name", notNullValue());
  }

  @Test
  void livenessUp() {
    given().when().get("/q/health/live").then().statusCode(200);
  }
}
