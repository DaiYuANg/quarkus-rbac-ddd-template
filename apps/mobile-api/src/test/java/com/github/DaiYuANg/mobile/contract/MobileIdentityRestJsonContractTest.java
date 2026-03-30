package com.github.DaiYuANg.mobile.contract;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.modules.identity.application.AuthApplicationService;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponse;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponseBuilder;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeRoleItem;
import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationToken;
import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationTokenBuilder;
import com.github.DaiYuANg.testsupport.QuarkusPostgresValkeyTestProfile;
import com.github.DaiYuANg.testsupport.ValkeyTestResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Same contract guarantees as admin identity JSON, for the mobile process base paths and cookies.
 */
@QuarkusTest
@QuarkusTestResource(ValkeyTestResource.class)
@TestProfile(QuarkusPostgresValkeyTestProfile.class)
class MobileIdentityRestJsonContractTest {

  @InjectMock AuthApplicationService authApplicationService;

  @Test
  void mobileLoginUsesIsolatedCookiePathAndStableTokenJson() {
    when(authApplicationService.login(any()))
        .thenReturn(
            SystemAuthenticationTokenBuilder.builder()
                .accessToken("ma")
                .refreshToken("mr")
                .tokenType("Bearer")
                .expiresIn(60L)
                .authorityVersion("mv")
                .build());

    given()
        .contentType("application/json")
        .body("{\"username\":\"member-1\",\"password\":\"root\"}")
        .when()
        .post("/api/mobile/v1/auth/login")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data", notNullValue())
        .body("data.accessToken", equalTo("ma"))
        .body("data.refreshToken", equalTo("mr"))
        .body("data.tokenType", equalTo("Bearer"))
        .body("data.expiresIn", equalTo(60))
        .body("data.authorityVersion", equalTo("mv"))
        .header("Set-Cookie", containsString("Path=/api/mobile/v1/auth"));
  }

  @Test
  @TestSecurity(
      user = "member-1",
      roles = {"member"})
  void mobileMeMatchesAdminMeEnvelope() {
    when(authApplicationService.me("member-1"))
        .thenReturn(
            MeResponseBuilder.builder()
                .id("m1")
                .name("Member")
                .email("m@x")
                .roles(List.of(new MeRoleItem("1", "member")))
                .permissions(Set.of())
                .build());

    given()
        .when()
        .get("/api/mobile/v1/me")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.id", equalTo("m1"))
        .body("data.name", equalTo("Member"))
        .body("data.email", equalTo("m@x"))
        .body("data.roles.size()", equalTo(1))
        .body("data.roles[0].id", equalTo("1"))
        .body("data.roles[0].name", equalTo("member"))
        .body("data.permissions.size()", equalTo(0));
  }
}
