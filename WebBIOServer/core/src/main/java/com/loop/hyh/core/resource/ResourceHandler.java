package com.loop.hyh.core.resource;

import com.loop.hyh.core.constant.CharsetProperties;
import com.loop.hyh.core.enumeration.HTTPStatus;
import com.loop.hyh.core.exception.RequestParseException;
import com.loop.hyh.core.exception.ResourceNotFoundException;
import com.loop.hyh.core.exception.base.ServletException;
import com.loop.hyh.core.exception.handler.ExceptionHandler;
import com.loop.hyh.core.request.Request;
import com.loop.hyh.core.response.Response;
import com.loop.hyh.core.template.TemplateResolver;
import com.loop.hyh.core.util.IOUtil;
import com.loop.hyh.core.util.MimeTypeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class ResourceHandler {
    private ExceptionHandler exceptionHandler;

    public ResourceHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
    
    public void handle(Request request, Response response, Socket client) {
        String url = request.getUrl();
        try {
            if (ResourceHandler.class.getResource(url) == null) {
                log.info("找不到该资源:{}",url);
                throw new ResourceNotFoundException();
            }
            String body = TemplateResolver.resolve(new String(IOUtil.getBytesFromFile(url), CharsetProperties.UTF_8_CHARSET),request);
            response.header(HTTPStatus.OK, MimeTypeUtil.getTypes(url)).body(body.getBytes(CharsetProperties.UTF_8_CHARSET)).write();
            log.info("{}已写入输出流", url);
        } catch (IOException e) {
            e.printStackTrace();
            exceptionHandler.handle(new RequestParseException(), response, client);
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, client);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}