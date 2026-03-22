package com.liangdian.api.dto.export;

import com.liangdian.export.annotation.ExcelColumn;

public record RoleExportRow(
    @ExcelColumn(header = "ID", order = 1) Long id,
    @ExcelColumn(header = "角色名称", order = 2) String name,
    @ExcelColumn(header = "角色编码", order = 3) String code,
    @ExcelColumn(header = "状态", order = 4) String status,
    @ExcelColumn(header = "排序", order = 5) Integer sort,
    @ExcelColumn(header = "权限组数量", order = 6) Integer permissionGroupCount
) {}
