package com.github.DaiYuANg.accesscontrol.parameter;

import com.github.DaiYuANg.common.model.PageQuery;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleQuery extends PageQuery {
  @QueryParam("name")
  private String name;
}
