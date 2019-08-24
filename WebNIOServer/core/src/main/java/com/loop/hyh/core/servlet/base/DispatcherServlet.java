package com.loop.hyh.core.servlet.base;

import com.loop.hyh.core.exception.base.ServletException;
import com.loop.hyh.core.exception.handler.ExceptionHandler;
import com.loop.hyh.core.resource.ResourceHandler;
import com.loop.hyh.core.response.Response;
import com.loop.hyh.core.server.WebApplication;
import com.loop.hyh.core.wrapper.NioSocketWrapper;
import com.loop.hyh.core.request.Request;
import com.loop.hyh.core.servlet.context.ServletContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
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
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Worker Pool-" + count++);
            }
        };
        this.pool = new ThreadPoolExecutor(100, 100, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(200), threadFactory,new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void shutdown() {
        pool.shutdown();
    }

    /**
     * 所有请求都经过DispatcherServlet的转发
     *
     * @throws IOException
     * @throws ServletException
     */
    public void doDispatch(NioSocketWrapper socketWrapper) {
        Request request = null;
        Response response = null;
        try {
            //解析请求
            request = new Request(socketWrapper.getSocketChannel());
            response = new Response(socketWrapper.getSocketChannel());
            request.setServletContext(servletContext);
            log.info("已经将请求放入worker线程池中");
            pool.execute(new RequestHandler(socketWrapper, request, response, servletContext.mapping(request.getUrl()), exceptionHandler, resourceHandler));
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, socketWrapper);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
