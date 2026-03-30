package com.github.DaiYuANg.common.exception;

import com.github.DaiYuANg.common.constant.ResultCode;
import java.util.Objects;
import lombok.Getter;
import lombok.val;

@Getter
public class BizException extends RuntimeException {
  private final ResultCode resultCode;

  public BizException(ResultCode resultCode) {
    this(resultCode, null);
  }

  public BizException(ResultCode resultCode, String message) {
    super(resolveMessage(resultCode, message));
    val code = requireCode(resultCode);
    this.resultCode = code;
  }

  private static ResultCode requireCode(ResultCode resultCode) {
    return Objects.requireNonNull(resultCode, "resultCode");
  }

  private static String resolveMessage(ResultCode resultCode, String message) {
    val code = requireCode(resultCode);
    return message != null ? message : code.message();
  }
}
