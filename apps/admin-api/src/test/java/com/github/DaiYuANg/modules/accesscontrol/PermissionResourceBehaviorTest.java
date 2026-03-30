package com.github.DaiYuANg.modules.accesscontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class PermissionResourceBehaviorTest {

  @Test
  void permissionResourceExposesReadOnlyEndpoints() {
    var resourceMethods =
        Stream.of(PermissionResource.class.getDeclaredMethods())
            .filter(this::isHttpEndpoint)
            .toList();

    assertEquals(4, resourceMethods.size());
    assertTrue(resourceMethods.stream().allMatch(method -> method.isAnnotationPresent(GET.class)));
    assertTrue(resourceMethods.stream().noneMatch(this::isMutatingEndpoint));
  }

  private boolean isHttpEndpoint(Method method) {
    return method.isAnnotationPresent(GET.class)
        || method.isAnnotationPresent(POST.class)
        || method.isAnnotationPresent(PUT.class)
        || method.isAnnotationPresent(PATCH.class)
        || method.isAnnotationPresent(DELETE.class);
  }

  private boolean isMutatingEndpoint(Method method) {
    return method.isAnnotationPresent(POST.class)
        || method.isAnnotationPresent(PUT.class)
        || method.isAnnotationPresent(PATCH.class)
        || method.isAnnotationPresent(DELETE.class);
  }
}
