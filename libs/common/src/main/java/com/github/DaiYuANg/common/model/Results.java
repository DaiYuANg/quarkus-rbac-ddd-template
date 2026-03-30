package com.github.DaiYuANg.common.model;

import com.github.DaiYuANg.common.constant.ResultCode;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.toolkit4j.data.model.envelope.Result;

@UtilityClass
public class Results {

  @Contract("_ -> new")
  public <T> @NonNull Result<String, T> ok(T data) {
    return Result.of(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.message(), data);
  }

  @Contract(" -> new")
  public @NonNull Result<String, Void> ok() {
    return Result.of(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.message());
  }

  @Contract("_ -> new")
  public <T> @NonNull Result<String, T> fail(@NonNull ResultCode code) {
    return Result.of(code.code(), code.message());
  }

  @Contract("_, _ -> new")
  public <T> @NonNull Result<String, T> fail(@NonNull ResultCode code, String message) {
    return Result.of(code.code(), message);
  }
}
