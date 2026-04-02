package com.github.DaiYuANg.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.Permission;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.PermissionGroup;
import com.github.DaiYuANg.security.config.SuperAdminAuthorityVersion;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.Role;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.User;
import com.github.DaiYuANg.testsupport.QuarkusPostgresValkeyTestProfile;
import com.github.DaiYuANg.testsupport.ValkeyTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * End-to-end HTTP flow: real {@code AuthApplicationService}, JWT, Redis, PostgreSQL
 * (Testcontainers). Uses the built-in <strong>super admin</strong> {@code root} / {@code root} (no
 * {@code sys_user} row).
 */
@QuarkusTest
@QuarkusTestResource(ValkeyTestResource.class)
@TestProfile(QuarkusPostgresValkeyTestProfile.class)
class AdminApiFullStackFlowIT {

  @Test
  void superAdminLoginAndMe() {
    String access = login("root", "root").accessToken();

    given()
        .header("Authorization", "Bearer " + access)
        .when()
        .get("/api/v1/me")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.id", equalTo("root"))
        .body("data.name", notNullValue())
        .body("data.permissions", notNullValue());
  }

  @Test
  void rootCanProvisionRbacFlowAndPaginationContracts() {
    long nonce = System.nanoTime();
    String rootAccess = login("root", "root").accessToken();

    Map<String, Long> permissionIds = permissionIdsByCode(rootAccess);
    long userViewPermissionId = permissionIds.get(User.VIEW);
    long roleViewPermissionId = permissionIds.get(Role.VIEW);

    long userGroupId =
        createPermissionGroup(
            rootAccess,
            "flow-group-user-" + nonce,
            "flow-group-user-" + nonce,
            "grants user:view");
    assignPermissions(rootAccess, userGroupId, List.of(userViewPermissionId));

    long roleGroupId =
        createPermissionGroup(
            rootAccess,
            "flow-group-role-" + nonce,
            "flow-group-role-" + nonce,
            "grants role:view");
    assignPermissions(rootAccess, roleGroupId, List.of(roleViewPermissionId));

    String roleCode = "flow-role-" + nonce;
    long roleId = createRole(rootAccess, "Flow Role " + nonce, roleCode);
    assignPermissionGroups(rootAccess, roleId, List.of(userGroupId));

    String pageKeyword = "flow-page-" + nonce;
    String pageUserA = pageKeyword + "-a";
    String pageUserB = pageKeyword + "-b";
    String pageUserC = pageKeyword + "-c";
    createUser(rootAccess, pageUserA, "password123", pageUserA + "@example.com");
    createUser(rootAccess, pageUserB, "password123", pageUserB + "@example.com");
    createUser(rootAccess, pageUserC, "password123", pageUserC + "@example.com");

    given()
        .header("Authorization", "Bearer " + rootAccess)
        .queryParam("page", 0)
        .queryParam("size", 2)
        .queryParam("keyword", pageKeyword)
        .queryParam("sortBy", "username")
        .queryParam("sortDirection", "asc")
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.page", equalTo(1))
        .body("data.size", equalTo(2))
        .body("data.totalElements", equalTo(3))
        .body("data.totalPages", equalTo(2))
        .body("data.content.size()", equalTo(2))
        .body("data.content.username", contains(pageUserA, pageUserB));

    given()
        .header("Authorization", "Bearer " + rootAccess)
        .queryParam("page", 1)
        .queryParam("size", 2)
        .queryParam("keyword", pageKeyword)
        .queryParam("sortBy", "username")
        .queryParam("sortDirection", "asc")
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.page", equalTo(2))
        .body("data.size", equalTo(2))
        .body("data.totalElements", equalTo(3))
        .body("data.totalPages", equalTo(2))
        .body("data.content.size()", equalTo(1))
        .body("data.content[0].username", equalTo(pageUserC));

    given()
        .header("Authorization", "Bearer " + rootAccess)
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("code", Role.VIEW)
        .when()
        .get("/api/v1/permission")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.page", equalTo(1))
        .body("data.size", equalTo(1))
        .body("data.totalElements", equalTo(1))
        .body("data.totalPages", equalTo(1))
        .body("data.content.size()", equalTo(1))
        .body("data.content[0].code", equalTo(Role.VIEW));

    String actorUsername = "flow-actor-" + nonce;
    long actorUserId =
        createUser(rootAccess, actorUsername, "password123", actorUsername + "@example.com");
    assignRoles(rootAccess, actorUserId, List.of(roleId));

    Session actorSession = login(actorUsername, "password123");

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", pageKeyword)
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.content.size()", equalTo(1));

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", roleCode)
        .when()
        .get("/api/v1/role")
        .then()
        .statusCode(403);

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", pageKeyword)
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.content.size()", equalTo(1));

    assignPermissionGroups(rootAccess, roleId, List.of(userGroupId, roleGroupId));

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", roleCode)
        .when()
        .get("/api/v1/role")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.page", equalTo(1))
        .body("data.totalElements", equalTo(1))
        .body("data.content[0].code", equalTo(roleCode));
  }

  @Test
  void rootCanProvisionUserAndPermissionChecksExpandAsExpected() {
    long nonce = System.nanoTime();
    String rootAccess = login("root", "root").accessToken();

    Map<String, Long> permissionIds = permissionIdsByCode(rootAccess);
    long userViewPermissionId = permissionIds.get(User.VIEW);
    long roleViewPermissionId = permissionIds.get(Role.VIEW);
    long permissionViewPermissionId = permissionIds.get(Permission.VIEW);
    long permissionGroupViewPermissionId = permissionIds.get(PermissionGroup.VIEW);

    long userViewGroupId =
        createPermissionGroup(
            rootAccess,
            "verify-group-user-view-" + nonce,
            "verify-group-user-view-" + nonce,
            "grants user:view");
    assignPermissions(rootAccess, userViewGroupId, List.of(userViewPermissionId));

    long roleViewGroupId =
        createPermissionGroup(
            rootAccess,
            "verify-group-role-view-" + nonce,
            "verify-group-role-view-" + nonce,
            "grants role:view");
    assignPermissions(rootAccess, roleViewGroupId, List.of(roleViewPermissionId));

    long permissionViewGroupId =
        createPermissionGroup(
            rootAccess,
            "verify-group-permission-view-" + nonce,
            "verify-group-permission-view-" + nonce,
            "grants permission:view");
    assignPermissions(rootAccess, permissionViewGroupId, List.of(permissionViewPermissionId));

    long permissionGroupViewGroupId =
        createPermissionGroup(
            rootAccess,
            "verify-group-permission-group-view-" + nonce,
            "verify-group-permission-group-view-" + nonce,
            "grants permission-group:view");
    assignPermissions(
        rootAccess, permissionGroupViewGroupId, List.of(permissionGroupViewPermissionId));

    String roleCode = "verify-role-" + nonce;
    long roleId = createRole(rootAccess, "Verify Role " + nonce, roleCode);
    assignPermissionGroups(rootAccess, roleId, List.of(userViewGroupId));

    String actorUsername = "verify-actor-" + nonce;
    long actorUserId =
        createUser(rootAccess, actorUsername, "password123", actorUsername + "@example.com");
    assignRoles(rootAccess, actorUserId, List.of(roleId));

    Session actorSession = login(actorUsername, "password123");

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", actorUsername)
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.content[0].username", equalTo(actorUsername));

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", roleCode)
        .when()
        .get("/api/v1/role")
        .then()
        .statusCode(403);

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", "verify-group")
        .when()
        .get("/api/v1/permission-group")
        .then()
        .statusCode(403);

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("code", Permission.VIEW)
        .when()
        .get("/api/v1/permission")
        .then()
        .statusCode(403);

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .contentType("application/json")
        .body(
            """
            {
              "username": "blocked-%s",
              "password": "password123",
              "nickname": "blocked-%s",
              "email": "blocked-%s@example.com",
              "userStatus": "ENABLED"
            }
            """
                .formatted(nonce, nonce, nonce))
        .when()
        .post("/api/v1/user")
        .then()
        .statusCode(403);

    assignPermissionGroups(
        rootAccess,
        roleId,
        List.of(
            userViewGroupId,
            roleViewGroupId,
            permissionViewGroupId,
            permissionGroupViewGroupId));

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", roleCode)
        .when()
        .get("/api/v1/role")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.content[0].code", equalTo(roleCode));

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 10)
        .queryParam("keyword", "verify-group")
        .when()
        .get("/api/v1/permission-group")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.totalElements", equalTo(4));

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("code", Permission.VIEW)
        .when()
        .get("/api/v1/permission")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.content[0].code", equalTo(Permission.VIEW));

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .contentType("application/json")
        .body(
            """
            {
              "name": "blocked-role-%s",
              "code": "blocked-role-%s",
              "status": "ENABLED",
              "sort": 1,
              "description": "should still be forbidden"
            }
            """
                .formatted(nonce, nonce))
        .when()
        .post("/api/v1/role")
        .then()
        .statusCode(403);
  }

  @Test
  void revokedPermissionsInvalidateExistingAccessTokenImmediately() {
    long nonce = System.nanoTime();
    String rootAccess = login("root", "root").accessToken();

    Map<String, Long> permissionIds = permissionIdsByCode(rootAccess);
    long userViewPermissionId = permissionIds.get(User.VIEW);

    long userViewGroupId =
        createPermissionGroup(
            rootAccess,
            "revoke-group-user-view-" + nonce,
            "revoke-group-user-view-" + nonce,
            "grants user:view");
    assignPermissions(rootAccess, userViewGroupId, List.of(userViewPermissionId));

    String roleCode = "revoke-role-" + nonce;
    long roleId = createRole(rootAccess, "Revoke Role " + nonce, roleCode);
    assignPermissionGroups(rootAccess, roleId, List.of(userViewGroupId));

    String actorUsername = "revoke-actor-" + nonce;
    long actorUserId =
        createUser(rootAccess, actorUsername, "password123", actorUsername + "@example.com");
    assignRoles(rootAccess, actorUserId, List.of(roleId));

    Session actorSession = login(actorUsername, "password123");

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", actorUsername)
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.content[0].username", equalTo(actorUsername));

    assignRoles(rootAccess, actorUserId, List.of());

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", actorUsername)
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(403);
  }

  @Test
  void disablingUserInvalidatesExistingAccessAndRefreshTokens() {
    long nonce = System.nanoTime();
    String rootAccess = login("root", "root").accessToken();

    Map<String, Long> permissionIds = permissionIdsByCode(rootAccess);
    long userViewPermissionId = permissionIds.get(User.VIEW);

    long userViewGroupId =
        createPermissionGroup(
            rootAccess,
            "disable-group-user-view-" + nonce,
            "disable-group-user-view-" + nonce,
            "grants user:view");
    assignPermissions(rootAccess, userViewGroupId, List.of(userViewPermissionId));

    String roleCode = "disable-role-" + nonce;
    long roleId = createRole(rootAccess, "Disable Role " + nonce, roleCode);
    assignPermissionGroups(rootAccess, roleId, List.of(userViewGroupId));

    String actorUsername = "disable-actor-" + nonce;
    long actorUserId =
        createUser(rootAccess, actorUsername, "password123", actorUsername + "@example.com");
    assignRoles(rootAccess, actorUserId, List.of(roleId));

    Session actorSession = login(actorUsername, "password123");

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", actorUsername)
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.content[0].username", equalTo(actorUsername));

    updateUserStatus(rootAccess, actorUserId, 0);

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", actorUsername)
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(401);

    given()
        .contentType("application/json")
        .body("{}")
        .header("X-Refresh-Token", actorSession.refreshToken())
        .when()
        .post("/api/v1/auth/refresh")
        .then()
        .statusCode(401)
        .body("code", equalTo("A0231"));
  }

  @Test
  void changingPasswordRevokesRefreshTokenButAccessTokenRemainsUsableUntilExpiry() {
    long nonce = System.nanoTime();
    String rootAccess = login("root", "root").accessToken();

    Map<String, Long> permissionIds = permissionIdsByCode(rootAccess);
    long userViewPermissionId = permissionIds.get(User.VIEW);

    long userViewGroupId =
        createPermissionGroup(
            rootAccess,
            "password-group-user-view-" + nonce,
            "password-group-user-view-" + nonce,
            "grants user:view");
    assignPermissions(rootAccess, userViewGroupId, List.of(userViewPermissionId));

    String roleCode = "password-role-" + nonce;
    long roleId = createRole(rootAccess, "Password Role " + nonce, roleCode);
    assignPermissionGroups(rootAccess, roleId, List.of(userViewGroupId));

    String actorUsername = "password-actor-" + nonce;
    long actorUserId =
        createUser(rootAccess, actorUsername, "password123", actorUsername + "@example.com");
    assignRoles(rootAccess, actorUserId, List.of(roleId));

    Session actorSession = login(actorUsername, "password123");

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", actorUsername)
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.content[0].username", equalTo(actorUsername));

    updateUserPassword(rootAccess, actorUserId, "changed456");

    given()
        .contentType("application/json")
        .body("{}")
        .header("X-Refresh-Token", actorSession.refreshToken())
        .when()
        .post("/api/v1/auth/refresh")
        .then()
        .statusCode(401)
        .body("code", equalTo("A0231"));

    given()
        .contentType("application/json")
        .body(
            """
            {
              "username": "%s",
              "password": "%s"
            }
            """
                .formatted(actorUsername, "password123"))
        .when()
        .post("/api/v1/auth/login")
        .then()
        .statusCode(401)
        .body("code", equalTo("A0210"));

    Session changedPasswordSession = login(actorUsername, "changed456");

    given()
        .header("Authorization", "Bearer " + changedPasswordSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", actorUsername)
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.content[0].username", equalTo(actorUsername));

    given()
        .header("Authorization", "Bearer " + actorSession.accessToken())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("keyword", actorUsername)
        .when()
        .get("/api/v1/user")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .body("data.content[0].username", equalTo(actorUsername));
  }

  @Test
  void livenessUp() {
    given().when().get("/q/health/live").then().statusCode(200);
  }

  private Session login(String username, String password) {
    Response response =
        given()
            .contentType("application/json")
            .body(
                """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """
                    .formatted(username, password))
            .when()
            .post("/api/v1/auth/login")
            .then()
            .statusCode(200)
            .body("code", equalTo("00000"))
            .body("data.accessToken", notNullValue())
            .body("data.refreshToken", notNullValue())
            .body("data.tokenType", equalTo("Bearer"))
            .header("Set-Cookie", notNullValue())
            .extract()
            .response();
    return new Session(response.path("data.accessToken"), response.path("data.refreshToken"));
  }

  private Map<String, Long> permissionIdsByCode(String accessToken) {
    List<Map<String, Object>> permissions =
        given()
            .header("Authorization", "Bearer " + accessToken)
            .when()
            .get("/api/v1/permission/list")
            .then()
            .statusCode(200)
            .body("code", equalTo("00000"))
            .extract()
            .path("data");
    return permissions.stream()
        .collect(
            java.util.stream.Collectors.toMap(
                permission -> String.valueOf(permission.get("code")),
                permission -> ((Number) permission.get("id")).longValue()));
  }

  private long createPermissionGroup(
      String accessToken, String name, String code, String description) {
    return given()
        .header("Authorization", "Bearer " + accessToken)
        .contentType("application/json")
        .body(
            """
            {
              "name": "%s",
              "description": "%s",
              "code": "%s",
              "sort": 1
            }
            """
                .formatted(name, description, code))
        .when()
        .post("/api/v1/permission-group")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .extract()
        .path("data.id");
  }

  private void assignPermissions(String accessToken, long permissionGroupId, List<Long> permissionIds) {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .contentType("application/json")
        .body(
            Map.of("permissionGroupId", permissionGroupId, "permissionIds", permissionIds))
        .when()
        .post("/api/v1/permission-group/assign/permission")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"));
  }

  private long createRole(String accessToken, String name, String code) {
    return given()
        .header("Authorization", "Bearer " + accessToken)
        .contentType("application/json")
        .body(
            """
            {
              "name": "%s",
              "code": "%s",
              "status": "ENABLED",
              "sort": 1,
              "description": "flow role"
            }
            """
                .formatted(name, code))
        .when()
        .post("/api/v1/role")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .extract()
        .path("data.id");
  }

  private void assignPermissionGroups(
      String accessToken, long roleId, List<Long> permissionGroupIds) {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .contentType("application/json")
        .body(Map.of("roleId", roleId, "permissionGroupIds", permissionGroupIds))
        .when()
        .post("/api/v1/role/assign/permission-group")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"));
  }

  private long createUser(String accessToken, String username, String password, String email) {
    return given()
        .header("Authorization", "Bearer " + accessToken)
        .contentType("application/json")
        .body(
            """
            {
              "username": "%s",
              "password": "%s",
              "nickname": "%s",
              "email": "%s",
              "userStatus": "ENABLED"
            }
            """
                .formatted(username, password, username, email))
        .when()
        .post("/api/v1/user")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"))
        .extract()
        .path("data.id");
  }

  private void assignRoles(String accessToken, long userId, List<Long> roleIds) {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .contentType("application/json")
        .body(Map.of("userId", userId, "roleIds", roleIds))
        .when()
        .post("/api/v1/user/assign/role")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"));
  }

  private void updateUserStatus(String accessToken, long userId, int status) {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .queryParam("status", status)
        .when()
        .put("/api/v1/user/" + userId + "/status")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"));
  }

  private void updateUserPassword(String accessToken, long userId, String newPassword) {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .contentType("application/json")
        .body(
            """
            {
              "newPassword": "%s"
            }
            """
                .formatted(newPassword))
        .when()
        .put("/api/v1/user/" + userId + "/password")
        .then()
        .statusCode(200)
        .body("code", equalTo("00000"));
  }

  private record Session(String accessToken, String refreshToken) {}
}
