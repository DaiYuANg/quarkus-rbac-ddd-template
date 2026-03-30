package com.github.DaiYuANg.modules.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.query.PermissionGroupPageQuery;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Getter
@Setter
public class PermissionGroupPageQueryParams
    extends AbstractAdminPageQueryParams<PermissionGroupPageQuery> {
  @QueryParam("name")
  private String name;

  public PermissionGroupPageQuery toQuery() {
    val query = applyTo(new PermissionGroupPageQuery());
    query.setName(name);
    return query;
  }
}
