package com.github.DaiYuANg.modules.security.runtime.request;

import com.github.DaiYuANg.security.request.RequestMetadata;
import com.github.DaiYuANg.security.request.RequestMetadataProvider;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;

@RequestScoped
public class JaxRsRequestMetadataProvider implements RequestMetadataProvider {
  @Context HttpHeaders headers;

  @Override
  public Optional<RequestMetadata> currentRequest() {
    if (headers == null) {
      return Optional.empty();
    }
    String remoteIp =
        firstHeader("X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP", "True-Client-IP");
    if (remoteIp != null && remoteIp.contains(",")) {
      remoteIp = remoteIp.split(",")[0].trim();
    }
    String userAgent = firstHeader("User-Agent");
    String requestId = firstHeader("X-Request-Id", "X-Correlation-Id");
    return Optional.of(
        new RequestMetadata(blankToNull(remoteIp), blankToNull(userAgent), blankToNull(requestId)));
  }

  private String firstHeader(@NonNull String... names) {
    return Stream.of(names)
        .map(headers::getRequestHeader)
        .filter(Objects::nonNull)
        .filter(values -> !values.isEmpty())
        .map(List::getFirst)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .findFirst()
        .orElse(null);
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
