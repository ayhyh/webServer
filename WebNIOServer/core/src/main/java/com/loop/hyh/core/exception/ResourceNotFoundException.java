package com.loop.hyh.core.exception;

import com.loop.hyh.core.enumeration.HTTPStatus;
import com.loop.hyh.core.exception.base.ServletException;

public class ResourceNotFoundException extends ServletException {
    private static final HTTPStatus status = HTTPStatus.NOT_FOUND;
    public ResourceNotFoundException() {
        super(status);
    }
}
