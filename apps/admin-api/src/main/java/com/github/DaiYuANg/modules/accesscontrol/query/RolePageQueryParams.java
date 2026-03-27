package com.github.DaiYuANg.modules.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.query.RolePageQuery;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePageQueryParams extends AbstractAdminPageQueryParams<RolePageQuery> {
  @QueryParam("name")
  private String name;

  public RolePageQuery toQuery() {
    var query = applyTo(new RolePageQuery());
    query.setName(name);
    return query;
  }
}
