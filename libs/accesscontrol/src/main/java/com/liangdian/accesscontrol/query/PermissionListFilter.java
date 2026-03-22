package com.liangdian.accesscontrol.query;

public record PermissionListFilter(String name, String code, String domain, String resource, String action, String groupCode) {}
