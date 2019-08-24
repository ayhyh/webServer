package com.loop.hyh.core.servlet.base;

import com.loop.hyh.core.enumeration.RequestMethod;
import com.loop.hyh.core.exception.base.ServletException;
import com.loop.hyh.core.response.Response;
import com.loop.hyh.core.request.Request;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
@Slf4j
public abstract class HTTPServlet {
    public void service(Request request, Response response) throws ServletException, IOException {
        if (request.getMethod() == RequestMethod.GET) {
            doGet(request, response);
        } else if (request.getMethod() == RequestMethod.POST) {
            doPost(request, response);
        } else if (request.getMethod() == RequestMethod.PUT) {
            doPut(request, response);
        } else if (request.getMethod() == RequestMethod.DELETE) {
            doDelete(request, response);
        }
    }

    public void doGet(Request request, Response response) throws ServletException, IOException {
    }

    public void doPost(Request request, Response response) throws ServletException, IOException {
    }

    public void doPut(Request request, Response response) throws ServletException, IOException {
    }

    public void doDelete(Request request, Response response) throws ServletException, IOException {
    }


}
