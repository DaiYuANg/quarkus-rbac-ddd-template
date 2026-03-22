package com.liangdian.common.model;

import java.util.List;

public record PageResult<T>(long total, int pageNum, int pageSize, List<T> records) {
    public static <T> PageResult<T> of(long total, int pageNum, int pageSize, List<T> records) {
        return new PageResult<>(total, pageNum, pageSize, records);
    }
}
