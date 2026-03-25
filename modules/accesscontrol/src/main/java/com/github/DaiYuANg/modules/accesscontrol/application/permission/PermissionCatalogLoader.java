package com.github.DaiYuANg.modules.accesscontrol.application.permission;

import com.github.DaiYuANg.accesscontrol.entity.SysPermission;
import com.github.DaiYuANg.accesscontrol.repository.PermissionRepository;
import com.github.DaiYuANg.cache.PermissionCatalogEntry;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

/**
 * Loads all permissions from DB into Redis at startup. Runtime permission reads then go through
 * Redis only.
 *
 * <p>Uses StartupEvent (after Flyway migrations and datasource init).
 */
@ApplicationScoped
@Slf4j
public class PermissionCatalogLoader {

  private final PermissionRepository permissionRepository;
  private final PermissionCatalogStore catalogStore;

  public PermissionCatalogLoader(
      PermissionRepository permissionRepository, PermissionCatalogStore catalogStore) {
    this.permissionRepository = permissionRepository;
    this.catalogStore = catalogStore;
  }

  void onStartup(@Observes StartupEvent event) {
    reload();
  }

  /** Reload catalog from DB into Redis. Call when catalog is empty or stale. */
  public void reload() {
    try {
      var all = permissionRepository.listAll();
      var entries = all.stream().map(this::toEntry).toList();
      catalogStore.loadAll(entries);
      log.info("Loaded {} permissions into Redis catalog", entries.size());
    } catch (Exception e) {
      log.error("Failed to load permission catalog into Redis", e);
      throw new IllegalStateException("Permission catalog bootstrap failed", e);
    }
  }

  @Contract("_ -> new")
  private @NonNull PermissionCatalogEntry toEntry(@NonNull SysPermission p) {
    return new PermissionCatalogEntry(
        p.id,
        p.name,
        p.code,
        p.resource,
        p.action,
        p.groupCode,
        p.description,
        p.expression,
        p.createAt,
        p.updateAt);
  }
}
