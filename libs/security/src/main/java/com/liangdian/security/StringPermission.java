package com.liangdian.security;

import java.security.Permission;

public class StringPermission extends Permission {
    public StringPermission(String name) {
        super(name);
    }

    @Override
    public boolean implies(Permission permission) {
        return permission != null && getName().equals(permission.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Permission other && getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String getActions() {
        return "";
    }
}
