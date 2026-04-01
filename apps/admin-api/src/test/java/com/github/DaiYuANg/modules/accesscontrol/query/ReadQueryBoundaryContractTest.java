package com.github.DaiYuANg.modules.accesscontrol.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.DaiYuANg.accesscontrol.query.PermissionGroupPageQuery;
import com.github.DaiYuANg.accesscontrol.query.PermissionPageQuery;
import com.github.DaiYuANg.accesscontrol.query.RolePageQuery;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.query.UserPageQuery;
import jakarta.ws.rs.QueryParam;
import java.util.Arrays;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;

class ReadQueryBoundaryContractTest {

  @Test
  void sharedReadQueriesDoNotCarryJaxRsQueryAnnotations() {
    assertNoQueryParams(ApiPageQuery.class);
    assertNoQueryParams(UserPageQuery.class);
    assertNoQueryParams(RolePageQuery.class);
    assertNoQueryParams(PermissionPageQuery.class);
    assertNoQueryParams(PermissionGroupPageQuery.class);
  }

  @Test
  void userQueryParamsMapToPureReadQuery() {
    val params = new UserPageQueryParams();
    params.setPageNum(2);
    params.setPageSize(30);
    params.setKeyword(" admin ");
    params.setSortBy("username");
    params.setSortDirection("desc");
    params.setUsername("alice");
    params.setUserStatus(UserStatus.ENABLED);

    val query = params.toQuery();

    assertEquals(2, query.getPageNum());
    assertEquals(30, query.getPageSize());
    assertEquals("admin", query.getKeyword());
    assertEquals("username", query.getSortBy());
    assertEquals("desc", query.getSortDirection());
    assertEquals("alice", query.getUsername());
    assertEquals(UserStatus.ENABLED, query.getUserStatus());
  }

  @Test
  void permissionQueryParamsMapToPureReadQuery() {
    val params = new PermissionPageQueryParams();
    params.setPage(1);
    params.setSize(15);
    params.setName("view");
    params.setCode("user:view");
    params.setResource("user");
    params.setAction("view");
    params.setGroupCode("user-management");

    val query = params.toQuery();

    assertEquals(2, query.getPageNum());
    assertEquals(15, query.getPageSize());
    assertEquals("view", query.getName());
    assertEquals("user:view", query.getCode());
    assertEquals("user", query.getResource());
    assertEquals("view", query.getAction());
    assertEquals("user-management", query.getGroupCode());
  }

  private void assertNoQueryParams(Class<?> type) {
    Stream.concat(
            Arrays.stream(type.getDeclaredFields()).map(field -> (AnnotatedElement & Member) field),
            Arrays.stream(type.getDeclaredMethods())
                .map(method -> (AnnotatedElement & Member) method))
        .forEach(
            member ->
                assertNull(
                    member.getAnnotation(QueryParam.class),
                    type.getName() + "#" + member.getName()));
  }
}
