package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.cache.config.RBACCacheProperties;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;

/**
 * Reference counting for shared role/permission Redis sets used by {@link UserAuthorityStore}.
 */
@ApplicationScoped
public class UserAuthorityRefCounter {

  private final ValueCommands<String, Long> refCountCommands;
  private final SetCommands<String, String> setCommands;
  private final KeyCommands<String> keyCommands;
  private final RBACCacheProperties props;

  public UserAuthorityRefCounter(RedisDataSource ds, RBACCacheProperties props) {
    this.refCountCommands = ds.value(Long.class);
    this.setCommands = ds.set(String.class);
    this.keyCommands = ds.key();
    this.props = props;
  }

  void adoptRoleSet(String roleHash, Set<String> roles) {
    if (roles == null || roles.isEmpty()) {
      return;
    }
    var roleHashKey = props.roleHashKey(roleHash);
    if (!keyCommands.exists(roleHashKey)) {
      setCommands.sadd(roleHashKey, roles.toArray(new String[0]));
      refCountCommands.set(props.roleRefCountKey(roleHash), 1L);
    } else {
      refCountCommands.incr(props.roleRefCountKey(roleHash));
    }
  }

  void adoptPermissionSet(String permissionHash, Set<String> permissions) {
    if (permissions == null || permissions.isEmpty()) {
      return;
    }
    var permissionHashKey = props.permissionHashKey(permissionHash);
    if (!keyCommands.exists(permissionHashKey)) {
      setCommands.sadd(permissionHashKey, permissions.toArray(new String[0]));
      refCountCommands.set(props.permissionRefCountKey(permissionHash), 1L);
    } else {
      refCountCommands.incr(props.permissionRefCountKey(permissionHash));
    }
  }

  void releaseRoleSet(String roleHash) {
    if (roleHash == null || roleHash.isBlank()) {
      return;
    }
    var roleHashKey = props.roleHashKey(roleHash);
    if (!keyCommands.exists(roleHashKey)) {
      return;
    }
    var refKey = props.roleRefCountKey(roleHash);
    var count = refCountCommands.decr(refKey);
    if (count <= 0) {
      keyCommands.del(roleHashKey);
      keyCommands.del(refKey);
    }
  }

  void releasePermissionSet(String permissionHash) {
    if (permissionHash == null || permissionHash.isBlank()) {
      return;
    }
    var permissionHashKey = props.permissionHashKey(permissionHash);
    if (!keyCommands.exists(permissionHashKey)) {
      return;
    }
    var refKey = props.permissionRefCountKey(permissionHash);
    var count = refCountCommands.decr(refKey);
    if (count <= 0) {
      keyCommands.del(permissionHashKey);
      keyCommands.del(refKey);
    }
  }
}
