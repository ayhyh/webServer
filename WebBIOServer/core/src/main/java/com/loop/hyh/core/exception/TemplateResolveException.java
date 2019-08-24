package com.loop.hyh.core.exception;

import com.loop.hyh.core.enumeration.HTTPStatus;
import com.loop.hyh.core.exception.base.ServletException;

public class TemplateResolveException extends ServletException {
    private static final HTTPStatus status = HTTPStatus.INTERNAL_SERVER_ERROR;
    public TemplateResolveException() {
        super(status);
    }
}
   
