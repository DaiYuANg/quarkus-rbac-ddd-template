package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.common.model.ApiPageQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionPageQuery extends ApiPageQuery {
  private String name;

  private String code;

  private String resource;

  private String action;

  private String groupCode;
}
