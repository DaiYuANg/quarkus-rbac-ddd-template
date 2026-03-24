package com.github.DaiYuANg.api.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AuthResourceContractTest {

  @Test
  void logoutEndpointRequiresAuthenticatedUser() {
    Method logout = findMethodByName("logout");

    assertNotNull(logout.getAnnotation(Authenticated.class));
    assertNull(logout.getAnnotation(PermitAll.class));
  }

  @Test
  void refreshEndpointUsesPostOnly() {
    Method refreshPost = findHttpMethod("auth-refresh", POST.class);
    Method refreshGet = findHttpMethod("auth-refresh", GET.class);

    assertNotNull(refreshPost);
    assertNotNull(refreshPost.getAnnotation(PermitAll.class));
    assertNull(refreshGet);
  }

  private Method findHttpMethod(String path, Class<?> httpAnnotation) {
    return Arrays.stream(AuthResource.class.getDeclaredMethods())
        .filter(
            method -> {
              Path p = method.getAnnotation(Path.class);
              if (p == null || !path.equals(trimLeadingSlash(p.value()))) {
                return false;
              }
              if (httpAnnotation == POST.class) {
                return method.isAnnotationPresent(POST.class);
              }
              if (httpAnnotation == GET.class) {
                return method.isAnnotationPresent(GET.class);
              }
              return false;
            })
        .findFirst()
        .orElse(null);
  }

  private Method findMethodByName(String methodName) {
    return Arrays.stream(AuthResource.class.getDeclaredMethods())
        .filter(method -> methodName.equals(method.getName()))
        .findFirst()
        .orElseThrow();
  }

  private String trimLeadingSlash(String value) {
    return value.startsWith("/") ? value.substring(1) : value;
  }
}
