package com.github.DaiYuANg.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.DaiYuANg.common.constant.ResultCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Result<T>(String code, String message, T data) {
    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.message(), data);
    }

    public static Result<Void> ok() {
        return new Result<>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.message(), null);
    }

    public static <T> Result<T> fail(ResultCode code) {
        return new Result<>(code.code(), code.message(), null);
    }

    public static <T> Result<T> fail(ResultCode code, String message) {
        return new Result<>(code.code(), message, null);
    }
}
