package com.github.DaiYuANg.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PageResult<T>(long total, int pageNum, int pageSize, List<T> records) {
    public static <T> PageResult<T> of(long total, int pageNum, int pageSize, List<T> records) {
        return new PageResult<>(total, pageNum, pageSize, records);
    }

    // Frontend contract compatibility aliases.
    @JsonProperty("items")
    public List<T> items() {
        return records;
    }

    @JsonProperty("page")
    public int page() {
        return pageNum;
    }

    @JsonProperty("pageSize")
    public int pageSizeAlias() {
        return pageSize;
    }
}
