package com.liangdian.api.handler;

import com.liangdian.common.constant.ResultCode;
import com.liangdian.common.exception.BizException;
import com.liangdian.common.model.Result;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof BizException biz) {
            return json(biz.getResultCode().status(), Result.fail(biz.getResultCode(), biz.getMessage()));
        }
        if (exception instanceof ConstraintViolationException violation) {
            var message = violation.getConstraintViolations().stream().findFirst().map(v -> v.getMessage()).orElse("validation failed");
            return json(ResultCode.BAD_REQUEST.status(), Result.fail(ResultCode.BAD_REQUEST, message));
        }
        if (exception instanceof UnauthorizedException) {
            return json(ResultCode.UNAUTHORIZED.status(), Result.fail(ResultCode.UNAUTHORIZED));
        }
        if (exception instanceof ForbiddenException) {
            return json(ResultCode.FORBIDDEN.status(), Result.fail(ResultCode.FORBIDDEN));
        }
        if (exception instanceof NotFoundException) {
            return json(ResultCode.NOT_FOUND.status(), Result.fail(ResultCode.NOT_FOUND));
        }
        return json(ResultCode.INTERNAL_ERROR.status(), Result.fail(ResultCode.INTERNAL_ERROR));
    }

    private Response json(Response.Status status, Object entity) {
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(entity).build();
    }
}
