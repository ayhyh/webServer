package com.loop.hyh.core.exception;

import com.loop.hyh.core.enumeration.HTTPStatus;
import com.loop.hyh.core.exception.base.ServletException;

public class RequestInvalidException extends ServletException {
    private static final HTTPStatus status = HTTPStatus.BAD_REQUEST;
    public RequestInvalidException() {
        super(status);
    }
}
