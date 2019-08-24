package com.loop.hyh.core.exception.handler;

import com.loop.hyh.core.exception.RequestInvalidException;
import com.loop.hyh.core.exception.base.ServletException;
import com.loop.hyh.core.response.Response;
import com.loop.hyh.core.util.IOUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

import static com.loop.hyh.core.constant.Context.ERROR_PAGE;

@Slf4j
public class ExceptionHandler {
    
    public void handle(ServletException e, Response response, Socket client) {
        try {
            if (e instanceof RequestInvalidException) {
                log.info("请求无法读取，丢弃");
            } else {
                log.info("抛出异常:{}", e.getClass().getName());
                e.printStackTrace();
                response
                        .header(e.getStatus())
                        .body(
                                IOUtil.getBytesFromFile(
                                        String.format(ERROR_PAGE, String.valueOf(e.getStatus().getCode())))
                        )
                        .write();
                log.info("错误消息已写入输出流");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
