package com.github.DaiYuANg.export.spi;

import com.github.DaiYuANg.export.model.ExportFile;
import com.github.DaiYuANg.export.model.ExportRequest;

public interface ExcelExporter {
    ExportFile export(ExportRequest request);
}
