package com.loop.hyh.core.request.dispatcher;

import com.loop.hyh.core.exception.base.ServletException;
import com.loop.hyh.core.request.Request;
import com.loop.hyh.core.response.Response;

import java.io.IOException;

public interface RequestDispatcher {
    
    void forward(Request request, Response response)  throws ServletException, IOException;
}
