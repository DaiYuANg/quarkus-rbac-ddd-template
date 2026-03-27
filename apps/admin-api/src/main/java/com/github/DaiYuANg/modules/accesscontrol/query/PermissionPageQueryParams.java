package com.github.DaiYuANg.modules.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.query.PermissionPageQuery;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionPageQueryParams extends AbstractAdminPageQueryParams<PermissionPageQuery> {
  @QueryParam("name")
  private String name;

  @QueryParam("code")
  private String code;

  @QueryParam("resource")
  private String resource;

  @QueryParam("action")
  private String action;

  @QueryParam("groupCode")
  private String groupCode;

  public PermissionPageQuery toQuery() {
    var query = applyTo(new PermissionPageQuery());
    query.setName(name);
    query.setCode(code);
    query.setResource(resource);
    query.setAction(action);
    query.setGroupCode(groupCode);
    return query;
  }
}
