package com.github.DaiYuANg.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

@ApplicationScoped
public class PermissionCatalogLocalCache {
  private static final String CACHE_KEY = "permission:catalog:all";

  private final Cache<String, List<PermissionCatalogEntry>> localCache =
      Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(1).build();

  public Optional<List<PermissionCatalogEntry>> getAll() {
    return Optional.ofNullable(localCache.getIfPresent(CACHE_KEY));
  }

  public void putAll(@NonNull List<PermissionCatalogEntry> permissions) {
    localCache.put(CACHE_KEY, permissions);
  }

  public void invalidate() {
    localCache.invalidate(CACHE_KEY);
  }
}
