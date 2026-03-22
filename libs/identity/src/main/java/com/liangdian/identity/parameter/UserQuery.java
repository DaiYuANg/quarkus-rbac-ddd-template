package com.liangdian.identity.parameter;

import com.liangdian.common.model.PageQuery;
import com.liangdian.identity.constant.UserStatus;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserQuery extends PageQuery {
    @QueryParam("username")
    private String username;

    @QueryParam("userStatus")
    private UserStatus userStatus;
}
