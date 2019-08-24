package com.loop.hyh.core.exception.handler;

import com.loop.hyh.core.exception.RequestInvalidException;
import com.loop.hyh.core.response.Header;
import com.loop.hyh.core.response.Response;
import com.loop.hyh.core.exception.base.ServletException;
import com.loop.hyh.core.wrapper.NioSocketWrapper;
import com.loop.hyh.core.util.IOUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.loop.hyh.core.constant.Context.ERROR_PAGE;

@Slf4j
public class ExceptionHandler {

    public void handle(ServletException e, Response response, NioSocketWrapper socketWrapper) {
        try {
            if (e instanceof RequestInvalidException) {
                log.info("请求无法读取，丢弃");
                socketWrapper.close();
            } else {
                log.info("抛出异常:{}", e.getClass().getName());
                e.printStackTrace();
                response.addHeader(new Header("Connection", "close"));
                response.setStatus(e.getStatus());
                response.setBody(IOUtil.getBytesFromFile(
                        String.format(ERROR_PAGE, String.valueOf(e.getStatus().getCode()))));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
