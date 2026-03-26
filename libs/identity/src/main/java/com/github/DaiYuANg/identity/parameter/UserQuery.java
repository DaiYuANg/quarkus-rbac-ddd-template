package com.github.DaiYuANg.identity.parameter;

import com.github.DaiYuANg.common.model.ApiPageQuery;
import com.github.DaiYuANg.identity.constant.UserStatus;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserQuery extends ApiPageQuery {
  @QueryParam("username")
  private String username;

  @QueryParam("userStatus")
  private UserStatus userStatus;
}
