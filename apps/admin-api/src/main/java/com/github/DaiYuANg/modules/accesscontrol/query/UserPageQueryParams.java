package com.github.DaiYuANg.modules.accesscontrol.query;

import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.query.UserPageQuery;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Getter
@Setter
public class UserPageQueryParams extends AbstractAdminPageQueryParams<UserPageQuery> {
  @QueryParam("username")
  private String username;

  @QueryParam("userStatus")
  private UserStatus userStatus;

  public UserPageQuery toQuery() {
    val query = applyTo(new UserPageQuery());
    query.setUsername(username);
    query.setUserStatus(userStatus);
    return query;
  }
}
