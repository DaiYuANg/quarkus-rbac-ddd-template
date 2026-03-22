package com.github.DaiYuANg.export.model;

import java.util.List;

public record ExportRequest(String sheetName, List<?> rows) {
    public ExportRequest {
        sheetName = sheetName == null || sheetName.isBlank() ? "Sheet1" : sheetName;
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
