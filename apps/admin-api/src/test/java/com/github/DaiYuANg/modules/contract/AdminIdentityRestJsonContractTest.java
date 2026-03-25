package com.github.DaiYuANg.modules.contract;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.modules.accesscontrol.application.permission.PermissionCatalogLoader;
import com.github.DaiYuANg.modules.identity.application.AuthApplicationService;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponse;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeRoleItem;
import com.github.DaiYuANg.modules.identity.application.dto.response.SystemAuthenticationToken;
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
 * Locks the stable JSON contract for admin identity endpoints. If these fail after a refactor, you
 * likely broke API consumers — update clients or version the API instead of weakening assertions.
 */
@QuarkusTest
@QuarkusTestResource(ValkeyTestResource.class)
@TestProfile(QuarkusPostgresValkeyTestProfile.class)
class AdminIdentityRestJsonContractTest {

  @InjectMock AuthApplicationService authApplicationService;
  @InjectMock PermissionCatalogLoader permissionCatalogLoader;

  @Test
  void loginWrapsTokenInResultEnvelopeWithStableFieldNames() {
    when(authApplicationService.login(any()))
        .thenReturn(new SystemAuthenticationToken("a", "r", "Bearer", 120L, "v-contract"));

    given()
        .contentType("application/json")
        .body("{\"username\":\"u\",\"password\":\"p\"}")
        .when()
        .post("/api/v1/auth/login")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data", notNullValue())
        .body("data.accessToken", equalTo("a"))
        .body("data.refreshToken", equalTo("r"))
        .body("data.tokenType", equalTo("Bearer"))
        .body("data.expiresIn", equalTo(120))
        .body("data.authorityVersion", equalTo("v-contract"));
  }

  @Test
  @TestSecurity(
      user = "u1",
      roles = {"r1"})
  void meExposesStableProfileShapeUnderData() {
    when(authApplicationService.me("u1"))
        .thenReturn(
            new MeResponse(
                "id-1", "N", "e@x", List.of(new MeRoleItem("rid", "rname")), Set.of("p1")));

    given()
        .when()
        .get("/api/v1/me")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.id", equalTo("id-1"))
        .body("data.name", equalTo("N"))
        .body("data.email", equalTo("e@x"))
        .body("data.roles.size()", equalTo(1))
        .body("data.roles[0].id", equalTo("rid"))
        .body("data.roles[0].name", equalTo("rname"))
        .body("data.permissions.size()", equalTo(1))
        .body("data.permissions[0]", equalTo("p1"));
  }
}
