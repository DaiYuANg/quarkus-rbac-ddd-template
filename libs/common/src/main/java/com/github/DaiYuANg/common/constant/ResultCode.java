package com.github.DaiYuANg.common.constant;

import jakarta.ws.rs.core.Response;

public enum ResultCode {
  SUCCESS("00000", "ok", Response.Status.OK),
  BAD_REQUEST("A0400", "bad request", Response.Status.BAD_REQUEST),
  UNAUTHORIZED("A0301", "unauthorized", Response.Status.UNAUTHORIZED),
  FORBIDDEN("A0300", "forbidden", Response.Status.FORBIDDEN),
  NOT_FOUND("A0606", "not found", Response.Status.NOT_FOUND),
  USER_ACCESS_BLOCKED("A0320", "user access blocked", Response.Status.FORBIDDEN),
  USERNAME_OR_PASSWORD_INVALID(
      "A0210", "username or password invalid", Response.Status.UNAUTHORIZED),
  REFRESH_TOKEN_INVALID("A0231", "refresh token invalid", Response.Status.UNAUTHORIZED),
  DATA_ALREADY_EXISTS("A0111", "data already exists", Response.Status.CONFLICT),
  DATA_NOT_FOUND("A0606", "data not found", Response.Status.NOT_FOUND),
  AUTHORIZATION_EXPIRED("A0311", "authorization expired", Response.Status.UNAUTHORIZED),
  ACCOUNT_TEMPORARILY_LOCKED(
      "A0211", "account temporarily locked", Response.Status.TOO_MANY_REQUESTS),
  INTERNAL_ERROR("B0001", "internal error", Response.Status.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final Response.Status status;

  ResultCode(String code, String message, Response.Status status) {
    this.code = code;
    this.message = message;
    this.status = status;
  }

  public String code() {
    return code;
  }

  public String message() {
    return message;
  }

  public Response.Status status() {
    return status;
  }
}
