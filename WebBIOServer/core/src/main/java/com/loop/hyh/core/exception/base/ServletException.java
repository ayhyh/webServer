package com.loop.hyh.core.exception.base;

import com.loop.hyh.core.enumeration.HTTPStatus;
import lombok.Getter;

@Getter
public class ServletException extends Exception {
    private HTTPStatus status;
    public ServletException(HTTPStatus status){
        this.status = status;
    }
}
