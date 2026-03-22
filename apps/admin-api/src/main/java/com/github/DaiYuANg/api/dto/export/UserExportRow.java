package com.github.DaiYuANg.api.dto.export;

import com.github.DaiYuANg.export.annotation.ExcelColumn;

public record UserExportRow(
    @ExcelColumn(header = "ID", order = 1) Long id,
    @ExcelColumn(header = "用户名", order = 2) String username,
    @ExcelColumn(header = "昵称", order = 3) String nickname,
    @ExcelColumn(header = "邮箱", order = 4) String email,
    @ExcelColumn(header = "手机号", order = 5) String mobilePhone,
    @ExcelColumn(header = "状态", order = 6) String status,
    @ExcelColumn(header = "角色数量", order = 7) Integer roleCount
) {}
