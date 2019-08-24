package com.loop.hyh.core.servlet.base;

import com.loop.hyh.core.enumeration.RequestMethod;
import com.loop.hyh.core.exception.ServerErrorException;
import com.loop.hyh.core.exception.ServletNotFoundException;
import com.loop.hyh.core.exception.base.ServletException;
import com.loop.hyh.core.exception.handler.ExceptionHandler;
import com.loop.hyh.core.resource.ResourceHandler;
import com.loop.hyh.core.response.Response;
import com.loop.hyh.core.wrapper.NioSocketWrapper;
import com.loop.hyh.core.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@Slf4j
public class RequestHandler implements Runnable {
    private NioSocketWrapper socketWrapper;
    private Request request;
    private Response response;
    private HTTPServlet servlet;
    private ExceptionHandler exceptionHandler;
    private ResourceHandler resourceHandler;
    
    
    /**
     * 在这里处理keep-alive
     */
    @Override
    public void run() {
        try {
            //为了让request能找得到response，以设置cookie
            request.setRequestHandler(this);
            //如果是静态资源，那么直接返回
            if (request.getMethod() == RequestMethod.GET && (request.getUrl().contains(".") || request.getUrl().equals("/"))) {
                log.info("静态资源:{}", request.getUrl());
                //首页
                if (request.getUrl().equals("/")) {
                    request.setUrl("/index.html");
                    resourceHandler.handle(request, response, socketWrapper);
                } else {
                    resourceHandler.handle(request, response, socketWrapper);
                }
            } else {
                if (servlet == null) {
                    throw new ServletNotFoundException();
                }
                //处理动态资源，交由某个Servlet执行
                //Servlet是单例多线程
                //Servlet在RequestHandler中执行
                servlet.service(request, response);
            }
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, socketWrapper);
        } catch (Exception e) {
            //其他未知异常
            exceptionHandler.handle(new ServerErrorException(), response, socketWrapper);
        } finally {
            response.writeToClient();
            List<String> connection = request.getHeaders().get("Connection");
            try {
                if (connection != null && connection.get(0).equals("close")) {
                    log.info("CLOSE: 客户端连接{} 已关闭", socketWrapper.getSocketChannel());
                    socketWrapper.close();
                } else {
                    // keep-alive 重新注册到Poller中
                    log.info("KEEP-ALIVE: 客户端连接{} 重新注册到Poller中", socketWrapper.getSocketChannel());
                    socketWrapper.getPoller().register(socketWrapper.getSocketChannel(),false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("请求处理完毕");
    }
}
