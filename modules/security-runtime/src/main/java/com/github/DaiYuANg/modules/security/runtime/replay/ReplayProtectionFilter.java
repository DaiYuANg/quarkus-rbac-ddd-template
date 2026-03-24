package com.github.DaiYuANg.modules.security.runtime.replay;

import com.github.DaiYuANg.cache.ReplayNonceStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.model.Result;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.time.Duration;
import java.time.Instant;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Settings use {@code app.replay.*} (see {@code application.yaml}); Redis uses existing {@code quarkus.redis.*} only.
 */
@Provider
@ReplayProtected
@Priority(Priorities.AUTHORIZATION + 50)
public class ReplayProtectionFilter implements ContainerRequestFilter {

  @ConfigProperty(name = "app.replay.enabled", defaultValue = "true")
  boolean enabled;

  @ConfigProperty(name = "app.replay.timestamp-header", defaultValue = "X-Timestamp")
  String timestampHeader;

  @ConfigProperty(name = "app.replay.nonce-header", defaultValue = "X-Nonce")
  String nonceHeader;

  @ConfigProperty(name = "app.replay.max-skew-seconds", defaultValue = "60")
  int maxSkewSeconds;

  @ConfigProperty(name = "app.replay.nonce-ttl-seconds", defaultValue = "120")
  int nonceTtlSeconds;

  @ConfigProperty(name = "app.replay.timestamp-epoch-seconds", defaultValue = "true")
  boolean timestampEpochSeconds;

  @ConfigProperty(name = "app.replay.max-nonce-length", defaultValue = "128")
  int maxNonceLength;

  @Inject ReplayNonceStore replayNonceStore;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (!enabled) {
      return;
    }
    if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
      return;
    }

    String tsRaw = requestContext.getHeaderString(timestampHeader);
    String nonce = requestContext.getHeaderString(nonceHeader);

    if (tsRaw == null || tsRaw.isBlank()) {
      abort(requestContext, "missing timestamp header");
      return;
    }
    if (nonce == null || nonce.isBlank()) {
      abort(requestContext, "missing nonce header");
      return;
    }
    if (nonce.length() > maxNonceLength) {
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

    Instant clientInstant =
        timestampEpochSeconds ? Instant.ofEpochSecond(parsed) : Instant.ofEpochMilli(parsed);
    Instant now = Instant.now();
    long skewSeconds = Duration.between(clientInstant, now).abs().getSeconds();
    if (skewSeconds > maxSkewSeconds) {
      abort(requestContext, "timestamp outside allowed skew");
      return;
    }

    Duration nonceTtl = Duration.ofSeconds(nonceTtlSeconds);
    if (!replayNonceStore.tryConsumeOnce(nonce, nonceTtl)) {
      abort(requestContext, "duplicate or invalid nonce");
    }
  }

  private static void abort(ContainerRequestContext ctx, String message) {
    ctx.abortWith(
        Response.status(ResultCode.BAD_REQUEST.status())
            .type(MediaType.APPLICATION_JSON)
            .entity(Result.fail(ResultCode.BAD_REQUEST, message))
            .build());
  }
}
