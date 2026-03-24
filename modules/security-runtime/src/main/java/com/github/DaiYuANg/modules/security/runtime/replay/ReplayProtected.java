package com.github.DaiYuANg.modules.security.runtime.replay;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires {@code app.replay.*} headers (default {@code X-Timestamp}, {@code X-Nonce}): timestamp within skew and a
 * fresh nonce (stored once in Redis via the default Quarkus Redis client).
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ReplayProtected {}
