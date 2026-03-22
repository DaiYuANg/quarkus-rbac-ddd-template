package com.github.DaiYuANg.api.dto.export;

import com.github.DaiYuANg.export.annotation.ExcelColumn;

public record PermissionGroupExportRow(
    @ExcelColumn(header = "ID", order = 1) Long id,
    @ExcelColumn(header = "权限组名称", order = 2) String name,
    @ExcelColumn(header = "权限组编码", order = 3) String code,
    @ExcelColumn(header = "排序", order = 4) Integer sort,
    @ExcelColumn(header = "描述", order = 5) String description,
    @ExcelColumn(header = "权限数量", order = 6) Integer permissionCount
) {}
