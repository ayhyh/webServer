package com.loop.hyh.core.servlet.base;

import com.loop.hyh.core.WebApplication;
import com.loop.hyh.core.enumeration.RequestMethod;
import com.loop.hyh.core.exception.base.ServletException;
import com.loop.hyh.core.exception.handler.ExceptionHandler;
import com.loop.hyh.core.resource.ResourceHandler;
import com.loop.hyh.core.response.Response;
import com.loop.hyh.core.request.Request;
import com.loop.hyh.core.servlet.context.ServletContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class DispatcherServlet {
    private ResourceHandler resourceHandler;
    private ExceptionHandler exceptionHandler;
    private ThreadPoolExecutor pool;
    private ServletContext servletContext;

    public DispatcherServlet() throws IOException {
        this.servletContext = WebApplication.getServletContext();
        this.exceptionHandler = new ExceptionHandler();
        this.resourceHandler = new ResourceHandler(exceptionHandler);
        this.pool = new ThreadPoolExecutor(5, 8, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void shutdown() {
        pool.shutdown();
    }

    /**
     * 所有请求都经过DispatcherServlet的转发
     *
     * @param client
     * @throws IOException
     * @throws ServletException
     */
    public void doDispatch(Socket client) throws IOException {
        Request request = null;
        Response response = null;
        try {
            //解析请求
            request = new Request(client.getInputStream());
            response = new Response(client.getOutputStream());
            request.setServletContext(servletContext);
            //如果是静态资源，那么直接返回
            if (request.getMethod() == RequestMethod.GET && (request.getUrl().contains(".") || request.getUrl().equals("/"))) {
                log.info("静态资源:{}", request);
                //首页
                if (request.getUrl().equals("/")) {
                    request.setUrl("/index.html");
                    resourceHandler.handle(request, response, client);
                }
                resourceHandler.handle(request, response, client);
            } else {
                //处理动态资源，交由某个Servlet执行
                //Servlet是单例多线程
                //Servlet在RequestHandler中执行
                pool.execute(new RequestHandler(client, request, response, servletContext.dispatch(request.getUrl()), exceptionHandler));
            }
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, client);
        }
    }
}
