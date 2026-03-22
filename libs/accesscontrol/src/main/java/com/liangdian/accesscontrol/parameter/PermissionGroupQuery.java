package com.liangdian.accesscontrol.parameter;

import com.liangdian.common.model.PageQuery;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionGroupQuery extends PageQuery {
    @QueryParam("name")
    private String name;
}
