package com.github.DaiYuANg.accesscontrol.parameter;

import com.github.DaiYuANg.common.model.PageQuery;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionQuery extends PageQuery {
    @QueryParam("name")
    private String name;

    @QueryParam("code")
    private String code;

    @QueryParam("domain")
    private String domain;

    @QueryParam("resource")
    private String resource;

    @QueryParam("action")
    private String action;

    @QueryParam("groupCode")
    private String groupCode;
}
