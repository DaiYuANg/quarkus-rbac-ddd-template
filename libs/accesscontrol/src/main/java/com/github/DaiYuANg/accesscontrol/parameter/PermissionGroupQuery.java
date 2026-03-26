package com.github.DaiYuANg.accesscontrol.parameter;

import com.github.DaiYuANg.common.model.ApiPageQuery;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionGroupQuery extends ApiPageQuery {
  @QueryParam("name")
  private String name;
}
