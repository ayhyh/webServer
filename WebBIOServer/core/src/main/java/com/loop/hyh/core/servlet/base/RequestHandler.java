package com.loop.hyh.core.servlet.base;

import com.loop.hyh.core.exception.ServerErrorException;
import com.loop.hyh.core.exception.ServletNotFoundException;
import com.loop.hyh.core.exception.base.ServletException;
import com.loop.hyh.core.exception.handler.ExceptionHandler;
import com.loop.hyh.core.response.Response;
import com.loop.hyh.core.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

/**
 * Servlet运行容器
 */
@Setter
@Getter
@AllArgsConstructor
@Slf4j
public class RequestHandler implements Runnable {
    private Socket client;
    private Request request;
    private Response response;
    private HTTPServlet servlet;
    private ExceptionHandler exceptionHandler;
    
    @Override
    public void run() {
        try {
            if (servlet == null) {
                throw new ServletNotFoundException();
            }
            //为了让request能找得到response，以设置cookie
            request.setRequestHandler(this);
            servlet.service(request, response);
            response.write();
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, client);
        } catch (Exception e) {
           //其他未知异常
            exceptionHandler.handle(new ServerErrorException(), response, client);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
