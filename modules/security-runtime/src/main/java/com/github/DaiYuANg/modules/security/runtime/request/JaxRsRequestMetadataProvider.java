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
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@RequestScoped
public class JaxRsRequestMetadataProvider implements RequestMetadataProvider {
  @Context HttpHeaders headers;

  @Override
  public Optional<RequestMetadata> currentRequest() {
    if (headers == null) {
      return Optional.empty();
    }
    val remoteIpHeader =
        firstHeader("X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP", "True-Client-IP");
    val remoteIp =
        remoteIpHeader != null && remoteIpHeader.contains(",")
            ? blankToNull(remoteIpHeader.split(",")[0])
            : blankToNull(remoteIpHeader);
    val userAgent = blankToNull(firstHeader("User-Agent"));
    val requestId = blankToNull(firstHeader("X-Request-Id", "X-Correlation-Id"));
    return Optional.of(
        new RequestMetadata(remoteIp, userAgent, requestId));
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
    return StringUtils.trimToNull(value);
  }
}
