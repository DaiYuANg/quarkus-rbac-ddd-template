package com.github.DaiYuANg.modules.security.runtime.identity;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotRefreshPolicy;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
@DefaultBean
@RequiredArgsConstructor
public class AdminPermissionSnapshotRefreshPolicy implements PermissionSnapshotRefreshPolicy {
  private static final Duration SNAPSHOT_TTL = Duration.ofHours(12);

  private final AuthorityVersionStore authorityVersionStore;

  @Override
  public boolean shouldReuse(String expectedAuthorityVersion, PermissionSnapshot cachedSnapshot) {
    if (cachedSnapshot == null) {
      return false;
    }
    // Reuse only if JWT version matches cached version AND cache is not stale (global bump)
    val cachedVersion = normalize(cachedSnapshot.authorityVersion());
    if (cachedVersion == null) {
      return false;
    }
    val expectedVersion = normalize(expectedAuthorityVersion);
    if (expectedVersion == null) {
      return true;
    }
    if (!expectedVersion.equals(cachedVersion)) {
      return false;
    }
    // Cached snapshot must match current global version (invalidated when admin bumps)
    return cachedVersion.equals(authorityVersionStore.currentVersion());
  }

  @Override
  public Duration snapshotTtl() {
    return SNAPSHOT_TTL;
  }

  @Override
  public String source() {
    return "valkey-snapshot";
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
