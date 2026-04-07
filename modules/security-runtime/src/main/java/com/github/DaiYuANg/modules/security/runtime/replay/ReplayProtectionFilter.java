package com.github.DaiYuANg.modules.security.runtime.replay;

import com.github.DaiYuANg.cache.ReplayNonceStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.model.Results;
import com.github.DaiYuANg.security.config.ReplayProtectionConfig;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.time.Duration;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

/**
 * Settings use {@code app.replay.*} (see {@code application.yaml}); Redis uses existing {@code
 * quarkus.redis.*} only.
 */
@Provider
@ReplayProtected
@Dependent
@Priority(Priorities.AUTHORIZATION + 50)
@RequiredArgsConstructor
public class ReplayProtectionFilter implements ContainerRequestFilter {
  private final ReplayProtectionConfig replayProtectionConfig;
  private final ReplayNonceStore replayNonceStore;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (!replayProtectionConfig.enabled()) {
      return;
    }
    if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
      return;
    }

    val tsRaw = normalizeHeader(requestContext.getHeaderString(replayProtectionConfig.timestampHeader()));
    val nonce = normalizeHeader(requestContext.getHeaderString(replayProtectionConfig.nonceHeader()));

    if (tsRaw == null) {
      abort(requestContext, "missing timestamp header");
      return;
    }
    if (nonce == null) {
      abort(requestContext, "missing nonce header");
      return;
    }
    if (nonce.length() > replayProtectionConfig.maxNonceLength()) {
      abort(requestContext, "nonce too long");
      return;
    }

    long parsed;
    try {
      parsed = Long.parseLong(tsRaw.trim());
    } catch (NumberFormatException e) {
      abort(requestContext, "invalid timestamp");
      return;
    }

    val clientInstant =
        replayProtectionConfig.timestampEpochSeconds()
            ? Instant.ofEpochSecond(parsed)
            : Instant.ofEpochMilli(parsed);
    val now = Instant.now();
    long skewSeconds = Duration.between(clientInstant, now).abs().getSeconds();
    if (skewSeconds > replayProtectionConfig.maxSkewSeconds()) {
      abort(requestContext, "timestamp outside allowed skew");
      return;
    }

    val nonceTtl = Duration.ofSeconds(replayProtectionConfig.nonceTtlSeconds());
    if (!replayNonceStore.tryConsumeOnce(nonce, nonceTtl)) {
      abort(requestContext, "duplicate or invalid nonce");
    }
  }

  private String normalizeHeader(String value) {
    return StringUtils.trimToNull(value);
  }

  private void abort(@NonNull ContainerRequestContext ctx, String message) {
    ctx.abortWith(
        Response.status(ResultCode.BAD_REQUEST.status())
            .type(MediaType.APPLICATION_JSON)
            .entity(Results.fail(ResultCode.BAD_REQUEST, message))
            .build());
  }
}
