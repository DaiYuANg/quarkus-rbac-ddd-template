package com.github.DaiYuANg.api.dto.export;

import com.github.DaiYuANg.export.annotation.ExcelColumn;

public record PermissionExportRow(
    @ExcelColumn(header = "ID", order = 1) Long id,
    @ExcelColumn(header = "权限名称", order = 2) String name,
    @ExcelColumn(header = "权限码", order = 3) String code,
    @ExcelColumn(header = "域", order = 4) String domain,
    @ExcelColumn(header = "资源", order = 5) String resource,
    @ExcelColumn(header = "动作", order = 6) String action,
    @ExcelColumn(header = "分组编码", order = 7) String groupCode,
    @ExcelColumn(header = "表达式", order = 8) String expression,
    @ExcelColumn(header = "描述", order = 9) String description
) {}
