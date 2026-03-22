package com.liangdian.export.spi;

import com.liangdian.export.model.ExportFile;
import com.liangdian.export.model.ExportRequest;

public interface ExcelExporter {
    ExportFile export(ExportRequest request);
}
