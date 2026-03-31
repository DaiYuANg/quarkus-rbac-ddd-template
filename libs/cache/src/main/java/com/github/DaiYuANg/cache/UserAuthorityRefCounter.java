package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.config.RBACCacheProperties;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/** Reference counting for shared role/permission Redis sets used by {@link UserAuthorityStore}. */
@ApplicationScoped
public class UserAuthorityRefCounter {

  private final ValueCommands<String, Long> refCountCommands;
  private final SetCommands<String, String> setCommands;
  private final KeyCommands<String> keyCommands;
  private final RBACCacheProperties props;

  public UserAuthorityRefCounter(@NonNull RedisDataSource ds, @NonNull RBACCacheProperties props) {
    this.refCountCommands = ds.value(Long.class);
    this.setCommands = ds.set(String.class);
    this.keyCommands = ds.key();
    this.props = props;
  }

  void adoptRoleSet(@NonNull String roleHash, Set<String> roles) {
    if (roles == null || roles.isEmpty()) {
      return;
    }
    val roleHashKey = props.roleHashKey(roleHash);
    if (!keyCommands.exists(roleHashKey)) {
      setCommands.sadd(roleHashKey, roles.toArray(new String[0]));
      refCountCommands.set(props.roleRefCountKey(roleHash), 1L);
    } else {
      refCountCommands.incr(props.roleRefCountKey(roleHash));
    }
  }

  void adoptPermissionSet(@NonNull String permissionHash, Set<String> permissions) {
    if (permissions == null || permissions.isEmpty()) {
      return;
    }
    val permissionHashKey = props.permissionHashKey(permissionHash);
    if (!keyCommands.exists(permissionHashKey)) {
      setCommands.sadd(permissionHashKey, permissions.toArray(new String[0]));
      refCountCommands.set(props.permissionRefCountKey(permissionHash), 1L);
    } else {
      refCountCommands.incr(props.permissionRefCountKey(permissionHash));
    }
  }

  void releaseRoleSet(String roleHash) {
    val normalizedRoleHash = normalize(roleHash);
    if (normalizedRoleHash == null) {
      return;
    }
    val roleHashKey = props.roleHashKey(normalizedRoleHash);
    if (!keyCommands.exists(roleHashKey)) {
      return;
    }
    val refKey = props.roleRefCountKey(normalizedRoleHash);
    val count = refCountCommands.decr(refKey);
    if (count <= 0) {
      keyCommands.del(roleHashKey);
      keyCommands.del(refKey);
    }
  }

  void releasePermissionSet(String permissionHash) {
    val normalizedPermissionHash = normalize(permissionHash);
    if (normalizedPermissionHash == null) {
      return;
    }
    val permissionHashKey = props.permissionHashKey(normalizedPermissionHash);
    if (!keyCommands.exists(permissionHashKey)) {
      return;
    }
    val refKey = props.permissionRefCountKey(normalizedPermissionHash);
    val count = refCountCommands.decr(refKey);
    if (count <= 0) {
      keyCommands.del(permissionHashKey);
      keyCommands.del(refKey);
    }
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
