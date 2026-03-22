package com.github.DaiYuANg.export.impl;

import com.github.DaiYuANg.export.annotation.ExcelColumn;
import com.github.DaiYuANg.export.model.ExportFile;
import com.github.DaiYuANg.export.model.ExportRequest;
import com.github.DaiYuANg.export.spi.ExcelExporter;
import jakarta.enterprise.context.ApplicationScoped;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stable exporter facade while the project migrates toward a real Apache Fesod workbook implementation.
 *
 * Current behavior intentionally keeps the endpoint usable with a CSV fallback.
 * Once the Fesod workbook API is wired in locally, only this class needs to change.
 */
@ApplicationScoped
public class FesodBackedExcelExporter implements ExcelExporter {
    @Override
    public ExportFile export(ExportRequest request) {
        var rows = request.rows();
        if (rows.isEmpty()) {
            return new ExportFile(request.sheetName() + ".csv", "text/csv", new byte[0]);
        }

        var orderedFields = orderedFields(rows.getFirst().getClass());
        var lines = new ArrayList<String>();
        lines.add(orderedFields.stream().map(FieldMeta::header).collect(Collectors.joining(",")));

        for (var row : rows) {
            lines.add(orderedFields.stream()
                .map(meta -> escape(readAsString(meta.field(), row)))
                .collect(Collectors.joining(",")));
        }

        var body = String.join("\n", lines);
        var content = ("﻿" + body).getBytes(StandardCharsets.UTF_8);
        return new ExportFile(request.sheetName() + ".csv", "text/csv; charset=UTF-8", content);
    }

    private List<FieldMeta> orderedFields(Class<?> type) {
        return java.util.Arrays.stream(type.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(ExcelColumn.class))
            .map(field -> {
                var annotation = field.getAnnotation(ExcelColumn.class);
                field.setAccessible(true);
                return new FieldMeta(field, annotation.header(), annotation.order());
            })
            .sorted(Comparator.comparingInt(FieldMeta::order))
            .toList();
    }

    private String readAsString(Field field, Object row) {
        try {
            var value = field.get(row);
            return value == null ? "" : String.valueOf(value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read export field: " + field.getName(), e);
        }
    }

    private String escape(String value) {
        var escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private record FieldMeta(Field field, String header, int order) {}
}
