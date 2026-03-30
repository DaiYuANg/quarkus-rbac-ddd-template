package com.github.DaiYuANg.mobile.integration;

import static io.restassured.RestAssured.given;

import com.github.DaiYuANg.testsupport.QuarkusPostgresValkeyTestProfile;
import com.github.DaiYuANg.testsupport.ValkeyTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

/** Mobile app full-stack smoke tests. User-side app does not expose a configured super-admin. */
@QuarkusTest
@QuarkusTestResource(ValkeyTestResource.class)
@TestProfile(QuarkusPostgresValkeyTestProfile.class)
class MobileApiFullStackFlowIT {

  @Test
  void meRequiresAuthentication() {
    given().when().get("/api/mobile/v1/me").then().statusCode(401);
  }

  @Test
  void livenessUp() {
    given().when().get("/q/health/live").then().statusCode(200);
  }
}
