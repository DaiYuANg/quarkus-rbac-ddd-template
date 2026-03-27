package com.github.DaiYuANg.identity.query;

import com.github.DaiYuANg.common.model.ApiPageQuery;
import com.github.DaiYuANg.identity.constant.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPageQuery extends ApiPageQuery {
  private String username;

  private UserStatus userStatus;
}
