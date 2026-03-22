package com.github.DaiYuANg.security;

public record RequestMetadata(
    String remoteIp,
    String userAgent,
    String requestId
) {
    public static RequestMetadata empty() {
        return new RequestMetadata(null, null, null);
    }
}
