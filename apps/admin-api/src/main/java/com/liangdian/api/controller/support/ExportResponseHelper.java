package com.liangdian.api.controller.support;

import com.liangdian.export.model.ExportFile;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

public final class ExportResponseHelper {
    private ExportResponseHelper() {
    }

    public static Response attachment(ExportFile file) {
        return Response.ok(file.content(), file.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.fileName())
                .build();
    }
}
