package com.github.DaiYuANg.common.exception;

import com.github.DaiYuANg.common.constant.ResultCode;
import java.util.Objects;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
  private final ResultCode resultCode;

  /**
   * Uses JDK 25+ flexible constructor bodies (statements before explicit {@code super} invocation).
   */
  public BizException(ResultCode resultCode) {
    var code = Objects.requireNonNull(resultCode, "resultCode");
    super(code.message());
    this.resultCode = code;
  }

  public BizException(ResultCode resultCode, String message) {
    var code = Objects.requireNonNull(resultCode, "resultCode");
    super(message != null ? message : code.message());
    this.resultCode = code;
  }
}
