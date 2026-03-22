package com.github.DaiYuANg.common.exception;

import com.github.DaiYuANg.common.constant.ResultCode;

public class BizException extends RuntimeException {
    private final ResultCode resultCode;

    public BizException(ResultCode resultCode) {
        super(resultCode.message());
        this.resultCode = resultCode;
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() { return resultCode; }
}
