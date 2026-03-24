package com.github.DaiYuANg.modules.security.runtime.request;

import com.github.DaiYuANg.security.request.RequestMetadata;
import com.github.DaiYuANg.security.request.RequestMetadataProvider;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@RequestScoped
public class JaxRsRequestMetadataProvider implements RequestMetadataProvider {
  @Context
  HttpHeaders headers;

  @Override
  public Optional<RequestMetadata> currentRequest() {
    if (headers == null) {
      return Optional.empty();
    }
    String remoteIp = firstHeader(
      "X-Forwarded-For",
      "X-Real-IP",
      "CF-Connecting-IP",
      "True-Client-IP"
    );
    if (remoteIp != null && remoteIp.contains(",")) {
      remoteIp = remoteIp.split(",")[0].trim();
    }
    String userAgent = firstHeader("User-Agent");
    String requestId = firstHeader("X-Request-Id", "X-Correlation-Id");
    return Optional.of(new RequestMetadata(blankToNull(remoteIp), blankToNull(userAgent), blankToNull(requestId)));
  }

  private @Nullable String firstHeader(String @NonNull ... names) {
    for (String name : names) {
      List<String> values = headers.getRequestHeader(name);
      if (values != null && !values.isEmpty()) {
        String value = values.getFirst();
        if (value != null && !value.isBlank()) {
          return value;
        }
      }
    }
    return null;
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
