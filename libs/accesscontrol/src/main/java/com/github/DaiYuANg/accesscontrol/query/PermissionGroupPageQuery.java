package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.common.model.ApiPageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionGroupPageQuery extends ApiPageQuery {
  private String name;
}
