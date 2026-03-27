package com.github.DaiYuANg.rest.support;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.common.model.Results;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(Exception exception) {
    if (!(exception instanceof BizException)) {
      log.error("Unhandled exception", exception);
    }
    return switch (exception) {
      case BizException biz ->
          json(biz.getResultCode().status(), Results.fail(biz.getResultCode(), biz.getMessage()));
      case ConstraintViolationException violation -> {
        var message =
            violation.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("validation failed");
        yield json(ResultCode.BAD_REQUEST.status(), Results.fail(ResultCode.BAD_REQUEST, message));
      }
      case UnauthorizedException ignored ->
          json(ResultCode.UNAUTHORIZED.status(), Results.fail(ResultCode.UNAUTHORIZED));
      case ForbiddenException ignored ->
          json(ResultCode.FORBIDDEN.status(), Results.fail(ResultCode.FORBIDDEN));
      case NotFoundException ignored ->
          json(ResultCode.NOT_FOUND.status(), Results.fail(ResultCode.NOT_FOUND));
      default -> json(ResultCode.INTERNAL_ERROR.status(), Results.fail(ResultCode.INTERNAL_ERROR));
    };
  }

  private static Response json(Response.Status status, Object entity) {
    return Response.status(status).type(MediaType.APPLICATION_JSON).entity(entity).build();
  }
}
