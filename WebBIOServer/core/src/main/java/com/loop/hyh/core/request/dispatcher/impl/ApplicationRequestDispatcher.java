package com.loop.hyh.core.request.dispatcher.impl;

import com.loop.hyh.core.enumeration.HTTPStatus;
import com.loop.hyh.core.exception.ResourceNotFoundException;
import com.loop.hyh.core.exception.base.ServletException;
import com.loop.hyh.core.request.Request;
import com.loop.hyh.core.request.dispatcher.RequestDispatcher;
import com.loop.hyh.core.resource.ResourceHandler;
import com.loop.hyh.core.response.Response;
import com.loop.hyh.core.template.TemplateResolver;
import com.loop.hyh.core.util.IOUtil;
import com.loop.hyh.core.util.MimeTypeUtil;
import com.loop.hyh.core.constant.CharsetProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ApplicationRequestDispatcher implements RequestDispatcher {
    private String url;
    
    @Override
    public void forward(Request request, Response response) throws ServletException, IOException {
        if (ResourceHandler.class.getResource(url) == null) {
            throw new ResourceNotFoundException();
        }
        log.info("forward至 {} 页面",url);
        String body = TemplateResolver.resolve(new String(IOUtil.getBytesFromFile(url), CharsetProperties.UTF_8_CHARSET),request);
        response.header(HTTPStatus.OK, MimeTypeUtil.getTypes(url)).body(body.getBytes(CharsetProperties.UTF_8_CHARSET));
    }
}
