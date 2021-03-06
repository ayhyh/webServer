package com.loop.hyh.core.request;

import com.loop.hyh.core.enumeration.RequestMethod;
import com.loop.hyh.core.exception.RequestInvalidException;
import com.loop.hyh.core.exception.RequestParseException;
import com.loop.hyh.core.model.Cookie;
import com.loop.hyh.core.model.HTTPSession;
import com.loop.hyh.core.servlet.base.RequestHandler;
import com.loop.hyh.core.servlet.context.ServletContext;
import com.loop.hyh.core.constant.CharConstant;
import com.loop.hyh.core.constant.CharsetProperties;
import com.loop.hyh.core.request.dispatcher.RequestDispatcher;
import com.loop.hyh.core.request.dispatcher.impl.ApplicationRequestDispatcher;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * GET /search?hl=zh-CN&source=hp&q=domety&aq=f&oq= HTTP/1.1
 * Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/vnd.ms-excel, application/vnd.ms-powerpoint,
 * application/msword, application/x-silverlight
 * Referer: <a href="http://www.google.cn/">http://www.google.cn/</a>
 * Accept-Language: zh-cn
 * Accept-Encoding: gzip, deflate
 * User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; TheWorld)
 * Host: <a href="http://www.google.cn">www.google.cn</a>
 * Connection: Keep-Alive
 * Cookie: PREF=ID=80a06da87be9ae3c:U=f7167333e2c3b714:NW=1:TM=1261551909:LM=1261551917:S=ybYcq2wpfefs4V9g;
 * NID=31=ojj8d-IygaEtSxLgaJmqSjVhCspkviJrB6omjamNrSm8lZhKy_yMfO2M4QMRKcH1g0iQv9u-2hfBW7bUFwVh7pGaRUb0RnHcJU37y-
 * FxlRugatx63JLv7CWMD6UB_O_r
 * <p>
 * POST /search HTTP/1.1
 * Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/vnd.ms-excel, application/vnd.ms-powerpoint,
 * application/msword, application/x-silverlight
 * Referer: <a href="http://www.google.cn/">http://www.google.cn/</a>
 * Accept-Language: zh-cn
 * Accept-Encoding: gzip,deflate
 * User-Agent: Mozilla/4.0(compatible;MSIE6.0;Windows NT5.1;SV1;.NET CLR2.0.50727;TheWorld)
 * Host: <a href="http://www.google.cn">www.google.cn</a>
 * Connection: Keep-Alive
 * Cookie: PREF=ID=80a06da87be9ae3c:U=f7167333e2c3b714:NW=1:TM=1261551909:LM=1261551917:S=ybYcq2wpfefs4V9g;
 * NID=31=ojj8d-IygaEtSxLgaJmqSjVhCspkviJrB6omjamNrSm8lZhKy_yMfO2M4QMRKcH1g0iQv9u-2hfBW7bUFwVh7pGaRUb0RnHcJU37y-
 * FxlRugatx63JLv7CWMD6UB_O_r
 * <p>
 * hl=zh-CN&source=hp&q=domety
 */

@Getter
@Setter
@Slf4j
public class Request {
    private RequestHandler requestHandler;
    private RequestMethod method;
    private String url;
    private Map<String, List<String>> params;
    private Map<String, List<String>> headers;
    private Map<String, Object> attributes;
    private ServletContext servletContext;
    private Cookie[] cookies;
    private HTTPSession session;
    
    /**
     * 读取请求体只能使用字节流，使用字符流读不到
     *
     * @param socketChannel
     * @throws RequestParseException
     */
    public Request(SocketChannel socketChannel) throws RequestParseException, RequestInvalidException, IOException {
        this.attributes = new HashMap<>();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        log.info("开始读取Request");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (socketChannel.read(buffer) > 0) {
            buffer.flip();
            baos.write(buffer.array());
        }
        baos.close();
        String[] lines = null;
        try {
            //支持中文，对中文进行URL解码
            lines = URLDecoder.decode(new String(baos.toByteArray(), CharsetProperties.UTF_8_CHARSET), CharsetProperties.UTF_8).split(CharConstant.CRLF);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        log.info("Request读取完毕");
        log.info("请求行: {}", Arrays.toString(lines));
        if (lines.length <= 1) {
            throw new RequestInvalidException();
        }
        try {
            parseHeaders(lines);
            if (headers.containsKey("Content-Length") && !headers.get("Content-Length").get(0).equals("0")) {
                parseBody(lines[lines.length - 1]);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RequestParseException();
        }
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public RequestDispatcher getRequestDispatcher(String url) {
        return new ApplicationRequestDispatcher(url);
    }

    /**
     * 如果请求报文中携带JSESSIONID这个Cookie，那么取出对应的session
     * 否则创建一个Session，并在响应报文中添加一个响应头Set-Cookie: JSESSIONID=D5A5C79F3C8E8653BC8B4F0860BFDBCD
     * <p>
     * 所有从请求报文中得到的Cookie，都会在响应报文中返回
     * <p>
     * 服务器只会在客户端第一次请求响应的时候，在响应头上添加Set-Cookie：“JSESSIONID=XXXXXXX”信息，
     * 接下来在同一个会话的第二第三次响应头里，是不会添加Set-Cookie：“JSESSIONID=XXXXXXX”信息的；
     * <p>
     * <p>
     * 即，如果在Cookie中读到的JSESSIONID，那么不会创建新的Session，也不会在响应头中加入Set-Cookie：“JSESSIONID=XXXXXXX”
     * 如果没有读到，那么会创建新的Session，并在响应头中加入Set-Cookie：“JSESSIONID=XXXXXXX”
     * 如果没有调用getSession，那么不会创建新的Session
     *
     * @return HTTPSession
     */
    public HTTPSession getSession() {
        if (session != null) {
            return session;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getKey().equals("JSESSIONID")) {
                HTTPSession currentSession = servletContext.getSession(cookie.getValue());
                if (currentSession != null) {
                    session = currentSession;
                    return session;
                }
            }
        }
        session = servletContext.createSession(requestHandler.getResponse());
        return session;
    }

    private void parseHeaders(String[] lines) {
        log.info("解析请求头");
        String firstLine = lines[0];
        //解析方法
        String[] firstLineSlices = firstLine.split(CharConstant.BLANK);
        this.method = RequestMethod.valueOf(firstLineSlices[0]);
        log.debug("method:{}", this.method);

        //解析URL
        String rawURL = firstLineSlices[1];
        String[] urlSlices = rawURL.split("\\?");
        this.url = urlSlices[0];
        log.debug("url:{}", this.url);

        //解析URL参数
        if (urlSlices.length > 1) {
            parseParams(urlSlices[1]);
        }
        log.debug("params:{}", this.params);

        //解析请求头
        String header;
        this.headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            header = lines[i];
            if (header.equals("")) {
                break;
            }
            int colonIndex = header.indexOf(':');
            String key = header.substring(0, colonIndex);
            String[] values = header.substring(colonIndex + 2).split(",");
            headers.put(key, Arrays.asList(values));
        }
        log.debug("headers:{}", this.headers);

        //解析Cookie

        if (headers.containsKey("Cookie")) {
            String[] rawCookies = headers.get("Cookie").get(0).split("; ");
            this.cookies = new Cookie[rawCookies.length];
            for (int i = 0; i < rawCookies.length; i++) {
                String[] kv = rawCookies[i].split("=");
                this.cookies[i] = new Cookie(kv[0], kv[1]);
            }
            headers.remove("Cookie");
        } else {
            this.cookies = new Cookie[0];
        }
        log.info("Cookies:{}", Arrays.toString(cookies));
    }

    private void parseBody(String body) {
        log.info("解析请求体");
        byte[] bytes = body.getBytes(CharsetProperties.UTF_8_CHARSET);
        List<String> lengths = this.headers.get("Content-Length");
        if(lengths != null){
            int length = Integer.parseInt(lengths.get(0));
            log.info("length:{}",length);
            parseParams(new String(bytes,0,length,CharsetProperties.UTF_8_CHARSET));
        }else{
            parseParams(body.trim());
        }
        if (this.params == null) {
            this.params = new HashMap<>();
        }
    }

    private void parseParams(String params) {
        String[] urlParams = params.split("&");
        if (this.params == null) {
            this.params = new HashMap<>();
        }
        for (String param : urlParams) {
            String[] kv = param.split("=");
            String key = kv[0];
            String[] values = kv[1].split(",");
            
            this.params.put(key, Arrays.asList(values));
        }
    }
}
